/*******************************************************************************
 * Copyright (c) 2012-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent.clojure;

import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$concat;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$conj;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$cons;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$drop;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$next;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$nthnext;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$rest;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$reverse;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$some;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$splitAt;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$take;

import java.util.Iterator;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.impl.AbstractList;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;

class List extends AbstractList {

	protected final Type et;
	protected final ISeq xs;
	
	protected List(Type et) {
		this(et, PersistentList.EMPTY);
	}
	
	protected List(IValue... values) {
		this(TypeInferenceHelper.lubFromVoid(values), seq(values));
	}
	
	static protected ISeq seq(IValue... values) {
		ISeq result = PersistentList.EMPTY;
		
		for(int i = values.length-1; i >= 0; i--) {
			result = result.cons(values[i]);
		}
		
		return result;
	}		
	
	protected List(Type et, ISeq xs) {
		this.et = et;
		this.xs = xs;
		
		if (et == null || xs == null) throw new NullPointerException();
	}
	
	private Type lub(IValue x) {
		return et.lub(x.getType());
	}

	private Type lub(IList xs) {
		return et.lub(xs.getElementType());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<IValue> iterator() {
		return ((Iterable<IValue>) xs).iterator();
	}

	@Override
	public Type getType() {
		return inferListOrRelType(getElementType(), this);
	}
	
	@Override
	public Type getElementType() {
		return et;
	}

	@Override
	public int length() {
		return xs.count();
	}

	@Override
	public IList reverse() {
		return new List(et, core$reverse(xs));
	}

	@Override
	public IList append(IValue x) {
		return new List(this.lub(x), appendAtSeq(xs, x));
	}

	@Override
	public IList insert(IValue x) {
		return new List(this.lub(x), (ISeq) xs.cons(x));
	}

	@Override
	public IList concat(IList other) {
		List that = (List) other;
		return new List(this.lub(that), core$concat(this.xs, that.xs));
	}

	@Override
	public IList put(int i, IValue x) throws FactTypeUseException,
			IndexOutOfBoundsException {
		/**
		 * Implementation requires (= contract) that i is a valid index. Note, that the
		 * index check doesn't exploit the full potential of lazy sequences. 
		 */
		if (i < 0 || i >= length()) throw new IndexOutOfBoundsException();
		return new List(this.lub(x), replaceInSeq(xs, i, x));
	}
	
	@Override
	public IValue get(int i) throws IndexOutOfBoundsException {
		if (i < 0 || i >= length()) throw new IndexOutOfBoundsException();
//		return (IValue) core$nth.invoke(xs, i);

		ISeq rest = xs;
		for (; i > 0; rest = rest.next(), i--);		
		return (IValue) rest.first();
	}

	@Override
	public IList sublist(int i, int n) {
		if (i < 0 || n < 0 || i + n > length()) throw new IndexOutOfBoundsException();
		return new List(et, core$take(n, core$drop(i, xs)));
	}

	@Override
	public boolean isEmpty() {
		return length() == 0;
	}

	@Override
	public boolean contains(IValue x) {
		/**
		 * @see http://clojure.github.com/clojure/clojure.core-api.html#clojure.core/some
		 */
		return !(null == core$some(PersistentHashSet.create(x), xs));
	}

	@Override
	public IList delete(IValue x) {
		return new List(et, deleteFromSeq(xs, x));
	}

	@Override
	public IList delete(int i) {
		if (i < 0 || i >= xs.count()) throw new IndexOutOfBoundsException();
		return new List(et, deleteFromSeq(xs, i));
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof List) {
			List that = (List) other;
			return this.xs.equals(that.xs);
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isEqual(IValue other) {
		return this.equals(other);
	}

	@Override
	public int hashCode() {
		return xs.hashCode();
	}


	/*
	 * Static Functions that operate on Clojure sequences.
	 * Are used herein and in ListWriter classes.
	 * 
	 * TODO: separate from this class.
	 */

	protected static Type lub(ISeq xs) {
		Type base = TypeFactory.getInstance().voidType();
		return xs == null ? base : lub(xs, base);
	}
	
	private static Type lub(ISeq xs, Type base) {
		Type result = base;
		
		while (xs != null && xs.first() != null) {
			result = result.lub(((IValue) xs.first()).getType());
			xs = xs.next();
		}

		return result;
	}		
	
	protected static ISeq appendAtSeq(ISeq xs, IValue x) {
		return core$reverse(core$conj(core$reverse(xs), x));		
	}
	
	protected static ISeq deleteFromSeq(ISeq xs, IValue x) {
		int i = 0;
		ISeq ys = xs;

		while (ys != null && ys.first() != null) {
			if (ys.first().equals(x)) {
				return core$concat(core$take(i, xs), ys.next());
			}
			
			i = i + 1;
			ys = ys.next();
		}
		
		return xs;
	}	
	
	protected static ISeq deleteFromSeq(ISeq xs, int i) {
		return core$concat(core$take(i, xs), core$rest(core$nthnext(xs, i)));
	}
	
	protected static ISeq replaceInSeq(ISeq xs, int i, IValue x) {
		IPersistentVector leftRight = core$splitAt(i, xs);
		ISeq newLeft = (ISeq) leftRight.nth(0);	
		ISeq newRight = (ISeq) core$cons.invoke(x, core$next((IPersistentCollection) leftRight.nth(1)));
		return core$concat(newLeft, newRight);
	}

	@Override
	protected IValueFactory getValueFactory() {
		return ValueFactory.getInstance();
	}
	
}

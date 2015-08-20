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

import java.util.Iterator;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.impl.AbstractList;
import org.eclipse.imp.pdb.facts.impl.func.ListFunctions;
import org.eclipse.imp.pdb.facts.type.Type;

import clojure.lang.IPersistentVector;
import clojure.lang.ITransientVector;
import clojure.lang.PersistentVector;

class Vector extends AbstractList {
	
	protected final Type ct;
	protected final IPersistentVector xs;
	
	protected Vector(IValue... values) {
		this(TypeInferenceHelper.lubFromVoid(values), PersistentVector.create((Object[])values));
	}

	protected Vector(Type et, IPersistentVector xs) {
		this.ct = inferListOrRelType(et, xs.count() == 0);
		this.xs = xs;
	}
	
	private Type lub(IValue x) {
		return getElementType().lub(x.getType());
	}

	private Type lub(IList xs) {
		return getElementType().lub(xs.getElementType());
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<IValue> iterator() {
		return ((Iterable<IValue>) xs).iterator();
	}

	@Override
	public Type getElementType() {
		return getType().getElementType();
	}
	
	@Override
	public Type getType() {
		return ct;
	}

	@Override
	public int length() {
		return xs.count();
	}

	@Override
	public IList reverse() {
		return new Vector(getElementType(), PersistentVector.create(xs.rseq()));
	}

	@Override
	public IList append(IValue newItem) {
		return new Vector(this.lub(newItem), (IPersistentVector) xs.cons(newItem));
	}

	@Override
	public IList insert(IValue newItem) {
		ITransientVector result = PersistentVector.EMPTY.asTransient();
		result = (ITransientVector) result.conj(newItem);
		for(Object item : (Iterable<?>) xs)
			result = (ITransientVector) result.conj(item);
		
		return new Vector(this.lub(newItem), (IPersistentVector) result.persistent());
	}

	@Override
	public IList concat(IList other) {
		Vector that = (Vector) other;
		
		ITransientVector result = null;
		
		if (this.xs instanceof PersistentVector) {
			// turn into transient
			result = ((PersistentVector) this.xs).asTransient();		
		} else {
			// perform eager copy
			result = PersistentVector.EMPTY.asTransient();

			for(Object item : (Iterable<?>) this.xs)
				result = (ITransientVector) result.conj(item);
		}
		
		for(Object item : (Iterable<?>) that.xs)
			result = (ITransientVector) result.conj(item);
		
		return new Vector(this.lub(that),
				(IPersistentVector) result.persistent());
	}

	@Override
	public IList put(int i, IValue x) throws FactTypeUseException,
			IndexOutOfBoundsException {
		/**
		 * Implementation requires (= contract) that i is a valid index. Note, that the
		 * index check doesn't exploit the full potential of lazy sequences. 
		 */
		if (i < 0 || i >= length()) throw new IndexOutOfBoundsException();
		return new Vector(this.lub(x), xs.assocN(i, x));
	}
	
	@Override
	public IValue get(int i) throws IndexOutOfBoundsException {	
		return (IValue) xs.nth(i);
	}

	@Override
	public IList sublist(int i, int n) {
		if (i < 0 || n < 0 || i + n > length()) throw new IndexOutOfBoundsException();
		return new Vector(getElementType(), clojure.lang.RT.subvec(xs, i, i+n));
	}

	@Override
	public boolean isEmpty() {
		return length() == 0;
	}

	@Override
	public IList delete(IValue x) {
		ITransientVector result = PersistentVector.EMPTY.asTransient();
		
		boolean skipped = false;
		for(Object item : (Iterable<?>) xs) {
			if (!skipped && item.equals(x)) {
				skipped = true;
			} else {
				result = (ITransientVector) result.conj(item);
			}
		}
				
		return new Vector(getElementType(), (IPersistentVector) result.persistent());
	}

	@Override
	public IList delete(int i) {
		if (i < 0 || i >= xs.count()) throw new IndexOutOfBoundsException();
		
		IPersistentVector result = PersistentVector.EMPTY;
		
		if (xs.count() != 1) {
			if (i == 0) {
				result = clojure.lang.RT.subvec(xs, 1, xs.count());
			} else if (i == xs.count() - 1) {
				result = clojure.lang.RT.subvec(xs, 0, xs.count() - 1);
			} else {
				IPersistentVector l = clojure.lang.RT.subvec(xs, 0, i);
				IPersistentVector r = clojure.lang.RT.subvec(xs, i+1, xs.count());				

				// perform eager copy
				ITransientVector transientResult = PersistentVector.EMPTY.asTransient();
				
				for(Object item : (Iterable<?>) l)
					transientResult = (ITransientVector) transientResult.conj(item);			

				for(Object item : (Iterable<?>) r)
					transientResult = (ITransientVector) transientResult.conj(item);
					
				result = (IPersistentVector) transientResult.persistent();
			}
		}
		
		return new Vector(getElementType(), (IPersistentVector) result);		
	}

	@Override
	public boolean equals(Object that) {
		return ListFunctions.equals(getValueFactory(), this, that);
	}

	@Override
	public boolean isEqual(IValue that) {
		return ListFunctions.isEqual(getValueFactory(), this, that);
	}
	
//	@Override
//	public boolean equals(Object other) {
//		if (other instanceof Vector) {
//			Vector that = (Vector) other;
//			return this.xs.equals(that.xs);
//		} else {
//			return false;
//		}
//	}
//
//	@Override
//	public boolean isEqual(IValue other) {
//		return this.equals(other);
//	}
	
	@Override
	public int hashCode() {
		return xs.hashCode();
	}
	
	@Override
	protected IValueFactory getValueFactory() {
		return ValueFactory.getInstance();
	}

}

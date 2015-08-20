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
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$drop;
import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$take;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.ITransientVector;
import clojure.lang.PersistentVector;
import clojure.lang.RT;

class VectorWriter implements IListWriter {

	protected final Type et;
	protected ITransientVector xs;
	
	protected VectorWriter(Type et){
		super();
		this.et = et;
		this.xs = PersistentVector.EMPTY.asTransient();
	}	
	
	@Override
	public void insertAll(Iterable<? extends IValue> values)
			throws FactTypeUseException {
		ITransientVector result = PersistentVector.EMPTY.asTransient();
		
		for(Object item : values)
			result = (ITransientVector) result.conj(item);

		for(Object item : (Iterable<?>) xs.persistent())
			result = (ITransientVector) result.conj(item);

		xs = result;
	}

	@Override
	public void insert(IValue... values) throws FactTypeUseException {
		ITransientVector result = PersistentVector.EMPTY.asTransient();
		
		for(Object item : values)
			result = (ITransientVector) result.conj(item);

		for(Object item : (Iterable<?>) xs.persistent())
			result = (ITransientVector) result.conj(item);

		xs = result;
	}

	@Override
	public void insertAt(int i, IValue... values)
			throws FactTypeUseException, IndexOutOfBoundsException {
		insertAt(i, RT.seq(values));
	}

	@Override
	public void insert(IValue[] values, int i, int n)
			throws FactTypeUseException, IndexOutOfBoundsException {
		insertAt(i, core$take(n, RT.seq(values)));
	}

	@Override
	public void insertAt(int i, IValue[] values, int j, int n)
			throws FactTypeUseException, IndexOutOfBoundsException {
		insertAt(i, (ISeq) core$take.invoke(n, core$drop.invoke(j, RT.seq(values))));
	}
	
	private void insertAt(int i, ISeq ys) {
		IPersistentVector tmp = (IPersistentVector) xs.persistent();
		xs = PersistentVector.create(core$concat(core$concat(core$take(i, tmp), ys), core$drop(i, tmp))).asTransient();
	}	

	// TODO / NOTE: inconsistency in naming; equivalent to List.put(i, x)
	@Override
	public void replaceAt(int i, IValue x) throws FactTypeUseException,
			IndexOutOfBoundsException {
		xs = xs.assocN(i, x);
	}

	@Override
	public void append(IValue... values) throws FactTypeUseException {
		for (IValue item : values) {
			xs = (ITransientVector) xs.conj(item);
		}
	}

	@Override
	public void appendAll(Iterable<? extends IValue> values)
			throws FactTypeUseException {
		for (IValue item : values) {
			xs = (ITransientVector) xs.conj(item);
		}
	}

	@Override
	public IList done() {
		return new Vector(et, (IPersistentVector) xs.persistent());
	}
	
}

class VectorWriterWithTypeInference extends VectorWriter {

	protected VectorWriterWithTypeInference() {
		super(TypeFactory.getInstance().voidType());
	}

	@Override
	// TODO: turns vector into sequence for type inference
	public IList done() {
		IPersistentVector resultVector = (IPersistentVector) xs.persistent();
		Type resultType = List.lub(resultVector.seq());
		return new Vector(resultType, resultVector);
	}
	  
}
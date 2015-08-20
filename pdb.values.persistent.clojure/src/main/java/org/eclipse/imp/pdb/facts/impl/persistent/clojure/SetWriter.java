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

import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import clojure.lang.IPersistentSet;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;

class SetWriter implements ISetWriter {

	protected final Type et;
	protected ITransientSet xs;
	
	protected SetWriter(Type et){
		super();
		this.et = et;
		this.xs = (ITransientSet) PersistentHashSet.EMPTY.asTransient();
	}
	
	@Override
	public void insert(IValue... values) throws FactTypeUseException {
		for (IValue x : values) {
			xs = (ITransientSet) xs.conj(x);
		}
	}

	@Override
	public void insertAll(Iterable<? extends IValue> values)
			throws FactTypeUseException {
		for (IValue x : values) {
			xs = (ITransientSet) xs.conj(x);
		}
	}

	@Override
	public ISet done() {
		IPersistentSet result = (IPersistentSet) xs.persistent();
		return new Set(et, result);
	}

}

class SetWriterWithTypeInference extends SetWriter {

	protected SetWriterWithTypeInference() {
		super(TypeFactory.getInstance().voidType());
	}

	@Override
	public ISet done() {
		IPersistentSet result = (IPersistentSet) xs.persistent();
		return new Set(List.lub(result.seq()), result);
	}
	  
}
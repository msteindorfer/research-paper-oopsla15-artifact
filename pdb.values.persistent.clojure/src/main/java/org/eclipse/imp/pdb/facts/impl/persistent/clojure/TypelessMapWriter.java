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

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;

import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;

class TypelessMapWriter implements IMapWriter {

	protected ITransientMap xs;

	protected TypelessMapWriter(){
		super();
		
		this.xs = PersistentHashMap.EMPTY.asTransient();
	}
	
	@Override
	public void insert(IValue... values) throws FactTypeUseException {
		for (IValue x : values) {
			ITuple t = (ITuple) x;
			xs = (ITransientMap) xs.assoc(t.get(0), t.get(1));
		}
	}

	@Override
	public void insertAll(Iterable<? extends IValue> values)
			throws FactTypeUseException {
		for (IValue x : values) {
			ITuple t = (ITuple) x;
			xs = (ITransientMap) xs.assoc(t.get(0), t.get(1));
		}
	}
	
	@Override
	public void put(IValue key, IValue value) throws FactTypeUseException {
		xs = (ITransientMap) xs.assoc(key, value);
	}

	@Override
	public void putAll(IMap map) throws FactTypeUseException {
		for (IValue k : map) {
			xs = (ITransientMap) xs.assoc(k, map.get(k));
		}
	}

	@Override
	public void putAll(java.util.Map<IValue, IValue> map) throws FactTypeUseException {
		for (IValue k : map.keySet()) {
			xs = (ITransientMap) xs.assoc(k, map.get(k));
		}
	}

	@Override
	public IMap done() {
		return new TypelessMap(xs.persistent());
	}

}
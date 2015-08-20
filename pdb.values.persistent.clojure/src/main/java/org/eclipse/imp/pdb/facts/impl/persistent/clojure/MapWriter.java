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
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import clojure.lang.IPersistentMap;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;

class MapWriter implements IMapWriter {

	protected final Type mapType;
	protected ITransientMap xs;

	protected MapWriter(Type mapType){
		super();
		this.mapType = mapType;
		this.xs = PersistentHashMap.EMPTY.asTransient();
	}
	
	protected MapWriter(Type kt, Type vt){
		super();
		this.mapType = TypeFactory.getInstance().mapType(kt, vt);
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
		for (IValue k: map) {
			xs = (ITransientMap) xs.assoc(k, map.get(k));
		}
	}

	@Override
	public void putAll(java.util.Map<IValue, IValue> map) throws FactTypeUseException {
		for (IValue k: map.keySet()) {
			xs = (ITransientMap) xs.assoc(k, map.get(k));
		}
	}

	@Override
	public IMap done() {
		IPersistentMap resultMap = xs.persistent();
		
		if (resultMap.count() == 0) {
			/*
			 * special treatment necessary if type contains labels
			 */
			Type voidType = TypeFactory.getInstance().voidType();
			Type voidMapType = TypeFactory.getInstance().mapType(voidType, mapType.getKeyLabel(), voidType, mapType.getValueLabel());
		
			return new Map(voidMapType, resultMap);
		} else {
			return new Map(mapType, resultMap);
		}
	}

}

class MapWriterWithTypeInference extends MapWriter {

	protected MapWriterWithTypeInference() {
		super(TypeFactory.getInstance().voidType(), TypeFactory.getInstance().voidType());
	}
	
	@Override
	public IMap done() {
		IPersistentMap resultMap = xs.persistent();
		Type resultKt = List.lub(ClojureHelper.core$keys(resultMap));
		Type resultVt = List.lub(ClojureHelper.core$vals(resultMap));
		
		return new Map(resultKt, resultVt, resultMap);
	}	
	
}
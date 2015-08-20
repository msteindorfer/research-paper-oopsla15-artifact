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

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

public class TypelessCljDsValueFactory extends org.eclipse.imp.pdb.facts.impl.fast.ValueFactory {

	/*package*/ TypelessCljDsValueFactory() {
		super();
	}	
	
	private static class InstanceKeeper {
		public final static TypelessCljDsValueFactory instance = new TypelessCljDsValueFactory();
	}
	
	public static TypelessCljDsValueFactory getInstance(){
		return InstanceKeeper.instance;
	}	
	
	@Override
	public IList list(IValue... values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IList list(Type et) {
		throw new UnsupportedOperationException();
	}
		
	@Override
	public IList listRelation(IValue... values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IList listRelation(Type tupleType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IListWriter listRelationWriter() {
		throw new UnsupportedOperationException();	
	}

	@Override
	public IListWriter listRelationWriter(Type et) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IListWriter listWriter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IListWriter listWriter(Type et) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMap map(Type mapType) {
		return mapWriter(mapType).done();
	}

	@Override
	public IMap map(Type kt, Type vt) {
		return mapWriter(kt, vt).done();
	}

	@Override
	public IMapWriter mapWriter() {
		return new TypelessCljDsMapWriter();
	}

	@Override
	public IMapWriter mapWriter(Type mapType) {
		return mapWriter();
	}

	@Override
	public IMapWriter mapWriter(Type kt, Type vt) {
		return mapWriter();
	}

	@Override
	public ISet relation(IValue... values) {
		return set(values);
	}

	@Override
	public ISet relation(Type et) {
//		return new Relation(et);
		return relationWriter(TypeFactory.getInstance().voidType()).done();
	}

	@Override
	public ISetWriter relationWriter() {
		return setWriter();
	}

	@Override
	public ISetWriter relationWriter(Type et) {
		return setWriter(et);
	}

	@Override
	public ISet set(IValue... values) {
		return new Set(values);
	}

	@Override
	public ISet set(Type et) {
		return set();
	}

	@Override
	public ISetWriter setWriter() {
		return new TypelessCljDsSetWriter();
	}

	@Override
	public ISetWriter setWriter(Type et) {
		return new SetWriter(et);
//		return new FastSetWriter(et);
	}

	@Override
	public ITuple tuple() {
		return new Tuple();
	}

	@Override
	public ITuple tuple(IValue... values) {
		return new Tuple(values);
	}

	@Override
	public ITuple tuple(Type type, IValue... values) {
		return new Tuple(type, values);
	}
	
	@Override
	public String toString() {
		return "VF_CLJ_DS";
	}
	
}

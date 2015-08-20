/*******************************************************************************
* Copyright (c) 2009 Centrum Wiskunde en Informatica (CWI)
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Arnold Lankamp - interfaces and implementation
*******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent.clojure;

import java.util.Iterator;

import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.util.collections.ShareableValuesHashSet;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import clojure.lang.IPersistentSet;
import clojure.lang.ITransientCollection;
import clojure.lang.PersistentHashSet;

// TODO Add checking.
/**
 * Implementation of ISetWriter.
 * 
 * @author Arnold Lankamp
 */
/*package*/ class FastSetWriter implements ISetWriter{
	protected Type elementType;
	protected final boolean inferred;
	
	protected final ShareableValuesHashSet data;
	
	protected ISet constructedSet;
	
	/*package*/ FastSetWriter(Type elementType){
		super();
		
		this.elementType = elementType;
		this.inferred = false;
		
		data = new ShareableValuesHashSet();
		
		constructedSet = null;
	}
	
	/*package*/ FastSetWriter(){
		super();
		
		this.elementType = TypeFactory.getInstance().voidType();
		this.inferred = true;
		
		data = new ShareableValuesHashSet();
		
		constructedSet = null;
	}

	/*package*/ FastSetWriter(Type elementType, ShareableValuesHashSet data){
		super();
		
		this.elementType = elementType;
		this.inferred = false;
		this.data = data;
		
		constructedSet = null;
	}
	
	public void insert(IValue value){
		checkMutation();
		updateType(value);
		data.add(value);
	}
	
	private void updateType(IValue value) {
		if (inferred) {
			elementType = elementType.lub(value.getType());
		}
	}

	public void insert(IValue... elements){
		checkMutation();
		
		for(int i = elements.length - 1; i >= 0; i--){
			updateType(elements[i]);
			data.add(elements[i]);
		}
	}
	
	public void insertAll(Iterable<? extends IValue> collection){
		checkMutation();
		
		Iterator<? extends IValue> collectionIterator = collection.iterator();
		while(collectionIterator.hasNext()){
			IValue next = collectionIterator.next();
			updateType(next);
			data.add(next);
		}
	}
	
	public void delete(IValue element){
		checkMutation();
		
		data.remove(element);
	}
	
	public int size(){
		return data.size();
	}

	protected void checkMutation(){
		if(constructedSet != null) throw new UnsupportedOperationException("Mutation of a finalized map is not supported.");
	}
	
	public ISet done(){
		ITransientCollection ret = PersistentHashSet.EMPTY.asTransient();
		for(Object item : data)
			ret = ret.conj(item);
		
		IPersistentSet persistentData = (IPersistentSet) ret.persistent();
		
		if (constructedSet == null) {
			constructedSet = new Set(elementType, persistentData);
		}
		
		return constructedSet;
	}
}
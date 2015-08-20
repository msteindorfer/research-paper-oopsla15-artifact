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
import java.util.Map.Entry;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.impl.AbstractMap;
import org.eclipse.imp.pdb.facts.type.Type;

import clojure.lang.APersistentMap;
import clojure.lang.IPersistentMap;

/*
 * Operates:
 * 		* without types
 * 		* with equals() instead of isEqual()
 */
public class TypelessMap extends AbstractMap {
	
	protected final IPersistentMap xs;		
	
	protected TypelessMap(IPersistentMap xs) {
		this.xs = xs;
	}
	
	@Override
	protected IValueFactory getValueFactory() {
		throw new UnsupportedOperationException();
	}
		
	@Override
	public Type getType() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int size() {
		return xs.count();
	}

	@Override
	public IMap put(IValue key, IValue value) {
		return new TypelessMap((IPersistentMap) xs.assoc(key, value));
	}

	@Override
	public IMap removeKey(IValue key) {
		return new TypelessMap((IPersistentMap) xs.without(key));
	}	
	
	@Override
	public IValue get(IValue key) {
		return (IValue) xs.valAt(key);
	}

	@Override
	public boolean containsKey(IValue key) {
		return xs.containsKey(key);
	}

	@Override
	public boolean containsValue(IValue value) {
		return ((APersistentMap) xs).containsValue(value);
	}
	
	@Override
	public int hashCode() {
		return xs.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other == null)
			return false;
		
		if (other instanceof TypelessMap) {
			TypelessMap that = (TypelessMap) other;

			return xs.equals(that.xs);
		}
		
		return false;
	}
	
	@Override
	public boolean isEqual(IValue other) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<IValue> iterator() {
		return ((APersistentMap) xs).keySet().iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<IValue> valueIterator() {
		return ((APersistentMap) xs).values().iterator();		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Entry<IValue, IValue>> entryIterator() {
		return ((APersistentMap) xs).entrySet().iterator();
	}

	@Override
	public IMap join(IMap that) {
		// TODO Auto-generated method stub
		return super.join(that);
	}

	@Override
	public IMap remove(IMap that) {
		// TODO Auto-generated method stub
		return super.remove(that);
	}

	@Override
	public IMap compose(IMap that) {
		// TODO Auto-generated method stub
		return super.compose(that);
	}

	@Override
	public IMap common(IMap that) {
		// TODO Auto-generated method stub
		return super.common(that);
	}

	@Override
	public boolean isSubMap(IMap that) {
		// TODO Auto-generated method stub
		return super.isSubMap(that);
	}
	
//	@Override
//	public IMap join(IMap other) {
//		TypelessMap that = (TypelessMap) other;
//		return new TypelessMap(core$merge(this.xs, that.xs));
//	}
//
//	@Override
//	public IMap remove(IMap other) {
//		ITransientMap transientResult = ((PersistentHashMap) xs).asTransient();
//		
//		for (IValue key: other) {
//			transientResult = transientResult.without(key);
//		}
//
//		return new TypelessMap(getType(), transientResult.persistent());
//	}
//
//	@Override
//	public IMap compose(IMap other) {
//		TypelessMap that = (TypelessMap) other;
//		IMapWriter writer;
//
//		if (this.getType().hasFieldNames() && that.getType().hasFieldNames()) {
//			/*
//			 * special treatment necessary if type contains labels
//			 */
//			Type newMapType = TypeFactory.getInstance().mapType(
//					this.getType().getKeyType(), 
//					this.getType().getKeyLabel(),
//					that.getType().getValueType(),
//					that.getType().getValueLabel());
//
//			writer = ValueFactory.getInstance().mapWriter(newMapType);
//		} else {
//			writer = ValueFactory.getInstance().mapWriter(this.getType().getKeyType(), that.getType().getValueType());
//		}		
//				
//		for (Iterator<Entry<IValue, IValue>> iterator = this.entryIterator(); iterator.hasNext();) {
//			Entry<IValue, IValue> pair = iterator.next();
//			
//			if (that.containsKey(pair.getValue()))
//				writer.put(pair.getKey(), that.get(pair.getValue()));
//		}
//		
//		return writer.done();
//	}
//
//	@Override
//	public IMap common(IMap other) {
//		TypelessMap that = (TypelessMap) other;
//		IMapWriter writer = ValueFactory.getInstance().mapWriter(getType().lub(other.getType()));
//		
//		for (IValue key : this) {
//			if (that.containsKey(key) && this.get(key).equals(that.get(key))) {
//				writer.put(key, this.get(key));
//			}
//		}
//		
//		return writer.done();
//	}
//
//	@Override
//	public boolean isSubMap(IMap other) {
//		TypelessMap that = (TypelessMap) other;
//		
//		for (IValue key: this) {
//			if (that.containsKey(key) == false) return false;
//			if (that.get(key).isEqual(this.get(key)) == false) return false;
//		}
//		
//		return true;
//	}	

}

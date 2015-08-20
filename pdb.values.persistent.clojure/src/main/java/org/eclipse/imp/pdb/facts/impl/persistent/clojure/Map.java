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

import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$merge;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.impl.AbstractMap;
import org.eclipse.imp.pdb.facts.impl.func.MapFunctions;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

import clojure.lang.APersistentMap;
import clojure.lang.IPersistentMap;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;

public class Map extends AbstractMap {
	
	protected final Type candidateMapType;
	protected final IPersistentMap xs;		
	
	protected Map(Type kt, Type vt, IPersistentMap xs) {
		// TODO: static candidate type might not match dynamic type
		this.candidateMapType = TypeFactory.getInstance().mapType(kt, vt);
		this.xs = xs;
	}
	
	protected Map(Type kt, Type vt) {
		// TODO: static candidate type might not match dynamic type
		this(kt, vt, PersistentHashMap.EMPTY);
	}
	
	protected Map(Type candidateMapType) {
		// TODO: static candidate type might not match dynamic type
		this(candidateMapType, PersistentHashMap.EMPTY);
	}	
	
	protected Map(Type candidateMapType, IPersistentMap xs) {
		// TODO: static candidate type might not match dynamic type
		this.candidateMapType = candidateMapType; 
		this.xs = xs;
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
		
		Type newMapType = getType();
		Type newKeyType = getType().getKeyType().lub(key.getType());
		Type newValueType = getType().getValueType().lub(value.getType());
		
		/*
		 * special treatment necessary if type contains labels
		 */
		if (newKeyType != getType().getKeyType()
				|| newValueType != getType().getValueType()) {

			newMapType = TypeFactory.getInstance().mapType(
					newKeyType,
					getType().getKeyLabel(), 
					newValueType,
					getType().getValueLabel());
		}	
		
		return new Map(newMapType, (IPersistentMap) xs.assoc(key, value));
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

//		Iterator<IValue> it = valueIterator();
//		
//		while (it.hasNext()) {
//			if (it.next().equals(value)) return true;
//		}
//		
//		return false;
				
//		Iterator<IMapEntry> it = xs.iterator();
//		
//		while (it.hasNext()) {
//			if (it.next().val().equals(value)) return true;
//		}
//		
//		return false;
	}

	@Override
	public Type getKeyType() {
		return getType().getKeyType();
	}

	@Override
	public Type getValueType() {
		return getType().getValueType();
	}

	@Override
	public Type getType() {
		return inferMapType(candidateMapType, isEmpty());
	}
	
	@Override
	public IMap join(IMap other) {
		Map that = (Map) other;
		return new Map(getType().lub(other.getType()), core$merge(this.xs, that.xs));
	}

	@Override
	public IMap remove(IMap other) {
		ITransientMap transientResult = ((PersistentHashMap) xs).asTransient();
		
		for (IValue key: other) {
			transientResult = transientResult.without(key);
		}

		return new Map(getType(), transientResult.persistent());
	}

	@Override
	public IMap compose(IMap other) {
		Map that = (Map) other;
		IMapWriter writer;

		if (this.getType().hasFieldNames() && that.getType().hasFieldNames()) {
			/*
			 * special treatment necessary if type contains labels
			 */
			Type newMapType = TypeFactory.getInstance().mapType(
					this.getType().getKeyType(), 
					this.getType().getKeyLabel(),
					that.getType().getValueType(),
					that.getType().getValueLabel());

			writer = ValueFactory.getInstance().mapWriter(newMapType);
		} else {
			writer = ValueFactory.getInstance().mapWriter(this.getType().getKeyType(), that.getType().getValueType());
		}		
				
		for (Iterator<Entry<IValue, IValue>> iterator = this.entryIterator(); iterator.hasNext();) {
			Entry<IValue, IValue> pair = iterator.next();
			
			if (that.containsKey(pair.getValue()))
				writer.put(pair.getKey(), that.get(pair.getValue()));
		}
		
		return writer.done();
	}

	@Override
	public IMap common(IMap other) {
		Map that = (Map) other;
		IMapWriter writer = ValueFactory.getInstance().mapWriter(getType().lub(other.getType()));
		
		for (IValue key : this) {
			if (that.containsKey(key) && this.get(key).equals(that.get(key))) {
				writer.put(key, this.get(key));
			}
		}
		
		return writer.done();
	}

	@Override
	public boolean isSubMap(IMap other) {
		Map that = (Map) other;
		
		for (IValue key: this) {
			if (that.containsKey(key) == false) return false;
			if (that.get(key).isEqual(this.get(key)) == false) return false;
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<IValue> iterator() {
		return ((APersistentMap) xs).keySet().iterator();
//		ISeq keys = core$keys(xs);
//		if (keys == null) 
//			return ((Iterable<IValue>) PersistentList.EMPTY).iterator();
//		else
//			return ((Iterable<IValue>) keys).iterator();			
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<IValue> valueIterator() {
		return ((APersistentMap) xs).values().iterator();		
//		ISeq vals = core$vals(xs);
//		if (vals == null) 
//			return ((Iterable<IValue>) PersistentList.EMPTY).iterator();
//		else
//			return ((Iterable<IValue>) vals).iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Entry<IValue, IValue>> entryIterator() {
		return ((APersistentMap) xs).entrySet().iterator();
//		return new Iterator<java.util.Map.Entry<IValue,IValue>>() {
//			
//			@SuppressWarnings("unchecked")
//			final Iterator<MapEntry> innerIterator = xs.iterator(); 
//			
//			@Override
//			public void remove() {
//				innerIterator.remove();
//			}
//			
//			@Override
//			public Entry<IValue, IValue> next() {
//				return new Entry<IValue, IValue>() {
//
//					final MapEntry innerEntry = innerIterator.next();
//					
//					@Override
//					public IValue getKey() {
//						return (IValue) innerEntry.getKey();
//					}
//
//					@Override
//					public IValue getValue() {
//						return (IValue) innerEntry.getValue();
//					}
//
//					@Override
//					public IValue setValue(IValue value) {
//						throw new UnsupportedOperationException();
//					}
//				};
//			}
//			
//			@Override
//			public boolean hasNext() {
//				return innerIterator.hasNext();
//			}
//		};
	}
	
	@Override
	public boolean equals(Object other) {
		return MapFunctions.equals(getValueFactory(), this, other);
	}

	@Override
	public boolean isEqual(IValue other) {
		return MapFunctions.isEqual(getValueFactory(), this, other);
	}
	
//	@Override
//	public boolean equals(Object other) {
//		if (other instanceof Map) {
//			Map that = (Map) other;
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

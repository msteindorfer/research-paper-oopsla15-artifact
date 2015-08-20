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

//import static org.eclipse.imp.pdb.facts.impl.persistent.clojure.ClojureHelper.core$merge;
//
//import java.util.Iterator;
//import java.util.Map;
//
//import org.eclipse.imp.pdb.facts.IConstructor;
//import org.eclipse.imp.pdb.facts.IList;
//import org.eclipse.imp.pdb.facts.INode;
//import org.eclipse.imp.pdb.facts.IValue;
//import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
//import org.eclipse.imp.pdb.facts.type.Type;
//import org.eclipse.imp.pdb.facts.type.TypeStore;
//import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
//
//import clojure.lang.IPersistentMap;
//import clojure.lang.IPersistentVector;
//import clojure.lang.PersistentHashMap;
//import clojure.lang.PersistentVector;
//
//public class Constructor extends Value implements IConstructor {
//
//	protected final IPersistentVector children;
//	protected final IPersistentMap annotations;	
//	
//	protected Constructor(Type constructorType, IPersistentVector children, IPersistentMap annotations) {
//		super(constructorType);
//		this.children = children;
//		this.annotations = annotations;
//	}
//	
//	protected Constructor(Type constructorType) {
//		this(constructorType, PersistentVector.EMPTY, PersistentHashMap.EMPTY);
//	}
//	
//	protected Constructor(Type constructorType, IValue... children) {
//		this(constructorType, PersistentVector.create((Object[])children), PersistentHashMap.EMPTY);
//	}	
//
//	protected Constructor(Type constructorType, java.util.Map<String, IValue> newAnnotationsMap, IValue... children) {
//		this(constructorType, PersistentVector.create((Object[])children), PersistentHashMap.create(newAnnotationsMap));
//	}	
//
//	@Override
//	public <T, E extends Throwable> T accept(IValueVisitor<T,E> v) throws E {
//		return v.visitConstructor(this);
//	}
//
//	@Override
//	public Type getType() {
//		return super.getType().getAbstractDataType();
//	}
//
//	@Override
//	public Type getConstructorType() {
//		return super.getType();
//	}
//
//	@Override
//	public IValue get(String label) {
//		return get(getConstructorType().getFieldIndex(label));
//	}
//
//	@Override
//	public IConstructor set(String label, IValue newChild)
//			throws FactTypeUseException {
//		return set (getConstructorType().getFieldIndex(label), newChild);
//	}
//
//	@Override
//	public boolean has(String label) {
//		return getConstructorType().hasField(label);
//	}
//
//	@Override
//	public IConstructor set(int i, IValue newChild)
//			throws FactTypeUseException {
//		return new Constructor(type, children.assocN(i, newChild), annotations);	
//	}
//
//	@Override
//	public Type getChildrenTypes() {
//		return getConstructorType().getFieldTypes();
//	}
//
//	@Override
//	public boolean declaresAnnotation(TypeStore store, String label) {
//		return store.getAnnotationType(getType(), label) != null;
//	}
//
//	@Override
//	public IConstructor setAnnotation(String label, IValue newValue)
//			throws FactTypeUseException {
//		return new Constructor(type, children, annotations.assoc(label, newValue));
//	}
//
//	@Override
//	public IConstructor joinAnnotations(Map<String, IValue> newAnnotationsMap) {
//		IPersistentMap newAnnotations = core$merge(annotations, PersistentHashMap.create(newAnnotationsMap));
//		return new Constructor(type, children, newAnnotations);
//	}
//
//	@Override
//	public IConstructor setAnnotations(Map<String, IValue> newAnnotations) {
//		return new Constructor(type, children, PersistentHashMap.create(newAnnotations));
//
//	}
//
//	@Override
//	public IConstructor removeAnnotations() {
//		return new Constructor(type, children, (IPersistentMap) annotations.empty());	
//	}
//
//	@Override
//	public IConstructor removeAnnotation(String key) {
//		return new Constructor(type, children, annotations.without(key));
//	}
//
//	@Override
//	public boolean equals(Object other) {
//		if (other instanceof Constructor) {
//			Constructor that = (Constructor) other;
//			return this.getConstructorType().equals(that.getConstructorType()) 
//					&& this.children.equiv(that.children);
////					&& this.annotations.equiv(that.annotations);
//		} else {
//			return false;
//		}
//	}
//	
//	@SuppressWarnings("rawtypes")
//	@Override
//	public int hashCode(){
//		int hash = getConstructorType().hashCode();
//		
//		for (Object child : (Iterable) children) {
//			hash = (hash << 1) ^ (hash >> 1) ^ child.hashCode();
//		}		
//		return hash;
//	}
//
//	@Override
//	public IValue get(int i) throws IndexOutOfBoundsException {
//		return (IValue) children.nth(i);
//	}
//
//	@Override
//	public int arity() {
//		return children.length();
//	}
//
//	@Override
//	public String getName() {
//		return getConstructorType().getName();
//	}
//	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public Iterable<IValue> getChildren() {
//		return (Iterable) children;
//	}
//
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public Iterator<IValue> iterator() {
//		return ((Iterable) children).iterator();
//	}
//
//	@Override
//	public IValue getAnnotation(String label) throws FactTypeUseException {
//		return (IValue) annotations.valAt(label, null);
//	}
//	
//	@Override
//	public boolean hasAnnotation(String label) throws FactTypeUseException {
//		return annotations.containsKey(label);
//	}
//
//	@Override
//	public boolean hasAnnotations() {
//		return !(annotations.count() == 0); 
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Map<String, IValue> getAnnotations() {
//		return (Map<String, IValue>) annotations;
//	}
//
//}

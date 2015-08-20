/*******************************************************************************
 * Copyright (c) 2012-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *    * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 ******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent.scala

import org.eclipse.imp.pdb.facts.IConstructor
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.TypeStore
import collection.JavaConversions.asJavaIterator
import collection.JavaConversions.mapAsJavaMap
import collection.JavaConversions.mapAsScalaMap
import org.eclipse.imp.pdb.facts.IList
import org.eclipse.imp.pdb.facts.IAnnotatable
import org.eclipse.imp.pdb.facts.impl.AbstractDefaultAnnotatable
import org.eclipse.imp.pdb.facts.util.ImmutableMap
import org.eclipse.imp.pdb.facts.impl.AnnotatedConstructorFacade

case class Constructor(val ct: Type, val children: Constructor.ChildrenColl) extends Value with IConstructor {
	
	def name: String = ct.getName

	def get(i: Int) = children(i)

	def arity = children.length

	def getName = name

	def getChildren = this

	def iterator = children.iterator

	def accept[T, E <: Throwable](v: IValueVisitor[T, E]): T = v visitConstructor this

	/*
	 * TODO: improve IConstructor.get(String) lookup time
	 * Example usage: (IConstructor) tree.get("prod")
	 */
	def get(label: String) = this get (t getFieldIndex label)

	def set(label: String, x: IValue) = this set(t getFieldIndex label, x)

	def has(label: String) = getConstructorType hasField label

	def getChildrenTypes = t.getFieldTypes

	def declaresAnnotation(store: TypeStore, label: String) = store.getAnnotationType(getType, label) != null


	def replace(first: Int, second: Int, end: Int, repl: IList) = ???

	def hasKeywordArguments: Boolean = ???

	def getKeywordArgumentNames: Array[String] = ???

	def getKeywordIndex(name: String): Int = ???

	def getKeywordArgumentValue(name: String): IValue = ???

	def positionalArity: Int = ???

	def getConstructorType = ct

	def getUninstantiatedConstructorType = ???

	override def t = ct.getAbstractDataType

	def set(i: Int, x: IValue) = Constructor(ct, children updated(i, x))
	
	override def isAnnotatable = true
	
	override def asAnnotatable: IAnnotatable[_ <: IConstructor] = {
		return new AbstractDefaultAnnotatable[IConstructor](this) {
			override def wrap(content: IConstructor, annotations: ImmutableMap[String, IValue]): IConstructor = {
				return new AnnotatedConstructorFacade(content, annotations);
			}
		};
	}
	
	override def mayHaveKeywordParameters(): Boolean = false

	override def asWithKeywordParameters(): org.eclipse.imp.pdb.facts.IWithKeywordParameters[IConstructor] = {
		throw new org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException(
				"Cannot be viewed as annotatable.", getType());
	}

}

object Constructor {
	
	type ChildrenColl = collection.immutable.List[IValue]
	val emptyChildren = collection.immutable.List.empty[IValue]

	def apply(ct: Type): Constructor = apply(ct, Constructor.emptyChildren)

}


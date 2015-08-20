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

import org.eclipse.imp.pdb.facts.ISet
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor
import collection.JavaConversions.asJavaIterator
import scala.collection.immutable.HashSet

case class TypelessSet(val xs: Set.Coll)
	extends Value with ISet {

	override def t = ???

	def getElementType = ???

	def isEmpty = xs.isEmpty

	def size = xs.size

	def contains(x: IValue) = xs contains x

	def insert(x: IValue): ISet = TypelessSet(xs + x)

	def delete(x: IValue): ISet = TypelessSet(xs - x)

	def union(other: ISet): ISet = other match {
		case TypelessSet(ys) => TypelessSet(xs | ys)
	}

	def intersect(other: ISet): ISet = other match {
		case TypelessSet(ys) => TypelessSet(xs & ys)
	}

	def subtract(other: ISet): ISet = other match {
		case TypelessSet(ys) => TypelessSet(xs &~ ys)
	}

	def product(other: ISet): ISet = ???
	
//	def product(other: ISet): ISet = other match {
//		case TypelessSet(ot, ys) => {
//			val tupleType = TypeFactory.getInstance tupleType(et, ot)
//			TypelessSet(tupleType, for (x <- xs; y <- ys) yield Tuple(tupleType, x, y))
//		}
//	}

	def isSubsetOf(other: ISet) = other match {
		case TypelessSet(ys) => xs subsetOf ys
	}

	def iterator = xs.iterator

	def accept[T, E <: Throwable](v: IValueVisitor[T, E]): T = v visitSet this

	override def equals(other: Any): Boolean = other match {
		case that: TypelessSet => this.xs equals that.xs
		case _ => false
	}

	override def hashCode = xs.hashCode

	def isRelation = getType.isRelation

	def asRelation = {
		import ImplicitRelationViewOnSet._
		this
	}

}

object TypelessSet {
	type Coll = collection.immutable.HashSet[IValue]
	val empty = collection.immutable.HashSet.empty[IValue]
}

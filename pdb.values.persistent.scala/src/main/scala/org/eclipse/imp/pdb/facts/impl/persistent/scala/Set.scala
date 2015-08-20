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

class Set(val et: Type, val xs: Set.Coll)
	extends Value with ISet {

	require(if (xs.isEmpty) et.isBottom else true)

	protected def lub(e: IValue) = et lub e.getType

	protected def lub(e: ISet) = et lub e.getElementType

	override val t = {
		if (et isTuple)
			TypeFactory.getInstance relTypeFromTuple et
		else
			TypeFactory.getInstance setType et
	}

	def getElementType = et

	def isEmpty = xs.isEmpty

	def size = xs.size

	def contains(x: IValue) = xs contains x

	def insert(x: IValue): ISet = Set(this lub x, xs + x)

	// TODO: lub is broken (does not shrink)
	def delete(x: IValue): ISet = Set(this lub x, xs - x)

	// TODO: higher order function with operation as parameter
	def union(other: ISet): ISet = other match {
		case Set(ot, ys) => {
			val rt = et lub ot
			val rv = xs | ys

			Set(rt, rv)
		}
	}

	// TODO: higher order function with operation as parameter
	def intersect(other: ISet): ISet = other match {
		case Set(ot, ys) => {
			val rt = et lub ot
			val rv = xs & ys

			Set(rt, rv)
		}
	}

	// TODO: higher order function with operation as parameter
	def subtract(other: ISet): ISet = other match {
		case Set(ot, ys) => {
			val rt = et // type is different from union and intersect
			val rv = xs &~ ys

			Set(rt, rv)
		}
	}

	def product(other: ISet): ISet = other match {
		case Set(ot, ys) => {
			val tupleType = TypeFactory.getInstance tupleType(et, ot)
			Set(tupleType, for (x <- xs; y <- ys) yield Tuple(tupleType, x, y))
		}
	}

	def isSubsetOf(other: ISet) = other match {
		case Set(_, ys) => xs subsetOf ys
	}

	def iterator = xs.iterator

	def accept[T, E <: Throwable](v: IValueVisitor[T, E]): T = {
		if (et isTuple)
			v visitRelation this
		else
			v visitSet this
	}

	override def equals(other: Any): Boolean = other match {
		case that: Set => (this.xs equals that.xs)
		case _ => false
	}

	override lazy val hashCode = xs.hashCode

	def isRelation = getType.isRelation

	def asRelation = {
		import ImplicitRelationViewOnSet._
		this
	}

}

object Set {
	type Coll = collection.immutable.HashSet[IValue]
	val empty = collection.immutable.HashSet.empty[IValue]

	def apply(et: Type, xs: Coll): ISet =
		new Set(if (xs isEmpty) TypeFactory.getInstance voidType else et, xs)

	def unapply(s: Set) = Some(s.et, s.xs)
}

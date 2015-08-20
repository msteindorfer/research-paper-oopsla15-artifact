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

import org.eclipse.imp.pdb.facts.IList
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor

import collection.JavaConversions.asJavaIterator

class List(val et: Type, val xs: ListColl)
	extends Value with IList {

	require(if (xs.isEmpty) et.isBottom else true)

	private def lub(e: IValue) = et lub e.getType

	private def lub(e: IList) = et lub e.getElementType

	override val t = {
		val elementType = if (xs isEmpty) TypeFactory.getInstance voidType else et

		if (elementType isTuple)
			TypeFactory.getInstance lrelTypeFromTuple elementType
		else
			TypeFactory.getInstance listType elementType
	}

	def getElementType = et

	def length = xs.length

	def reverse(): IList = List(et, xs.reverse)

	def append(x: IValue): IList = List(this lub x, xs :+ x)

	def insert(x: IValue): IList = List(this lub x, x +: xs)

	def concat(other: IList): IList = other match {
		case List(_, ys) => List(this lub other, xs ++ ys)
	}

	def put(i: Int, x: IValue): IList = List(this lub x, xs updated(i, x))

	def get(i: Int) = xs(i)

	def sublist(i: Int, n: Int): IList = {
		if (i < 0 || n < 0 || i + n > length) {
			throw new IndexOutOfBoundsException()
		} /* for compatibility with Rascal test suite */
		List(et, xs slice(i, i + n))
	}

	def isEmpty = xs.isEmpty

	def contains(e: IValue) = xs exists (_ == e)

	def delete(x: IValue): IList = xs indexOf x match {
		case i => if (i == -1) this.asInstanceOf[IList] else delete(i)
	}

	def delete(i: Int): IList = List(et, (xs take i) ++ (xs drop i + 1))

	def intersect(other: IList): IList = other match {
		case List(ot, ys) => {
			val rt = et lub ot
			val rv = for (x <- xs if ys exists (_ isEqual x)) yield x // xs intersect ys ??

			List(rt, rv)
		}
	}

	def subtract(other: IList): IList = other match {
		case List(ot, ys) => List(ot, xs diff ys)
	}

	def product(other: IList): IList = other match {
		case List(ot, ys) => {
			val productType = TypeFactory.getInstance tupleType(et, ot)
			List(productType, (for (x <- xs; y <- ys) yield Tuple(x, y)))
		}
	}

	// TODO: stop if iterator is exhausted
	// NOTE: uses mutable BufferedIterator
	def isSubListOf(other: IList): Boolean = other match {
		case List(ot, ys) => {
			val it = xs.iterator.buffered
			ys foreach ((y) => if (it.hasNext && it.head == y) it.next);
			it.isEmpty
		}
	}

	def replace(first: Int, second: Int, end: Int, repl: IList): IList = ???

	def iterator = xs.iterator

	def accept[T, E <: Throwable](v: IValueVisitor[T, E]): T = {
		if (et isTuple)
			v visitListRelation this
		else
			v visitList this
	}

	override def equals(that: Any): Boolean = that match {
		case other: List =>
			if (this.xs eq other.xs) true
			else {
				if (this.length == other.length) (this.xs equals other.xs)
				else false
			}
		case _ => false
	}

	override lazy val hashCode = xs.hashCode

	def isRelation = getType.isRelation

	def asRelation = {
		import ImplicitRelationViewOnList._
		this
	}

}

object List {
	def apply(et: Type, xs: ListColl): List =
		new List(if (xs isEmpty) TypeFactory.getInstance voidType else et, xs)

	def unapply(l: List) = Some(l.et, l.xs)
}

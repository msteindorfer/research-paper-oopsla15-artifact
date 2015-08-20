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

import org.eclipse.imp.pdb.facts.IMap
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor

import collection.JavaConversions.asJavaIterator
import collection.JavaConversions.mapAsJavaMap

case class TypelessMap(xs: TypelessMap.Coll)
	extends Value with IMap {

	override def t = ???

	override def isEmpty = xs isEmpty

	override def size = xs size

	override def put(k: IValue, v: IValue) = TypelessMap(xs + (k -> v))

	override def removeKey(k: IValue) = TypelessMap(xs - k)

	override def get(k: IValue) = xs getOrElse(k, null)

	override def containsKey(k: IValue) = xs contains k

	override def containsValue(v: IValue) = xs exists {
		case (_, cv) => v == cv
	}

	override def getKeyType = ???

	override def getValueType = ???

	override def join(other: IMap): IMap = other match {
		case TypelessMap(ys) => TypelessMap(xs ++ ys)
	}

	override def remove(other: IMap): IMap = other match {
		case TypelessMap(ys) => TypelessMap(xs -- ys.keySet)
	}

	override def compose(other: IMap): IMap = other match {
		case TypelessMap(ys) => TypelessMap(for ((k, v) <- xs if ys contains v) yield (k, ys(v)))
	}

	override def common(other: IMap) = other match {
		case TypelessMap(ys) =>
		 TypelessMap(
				xs filter {
					case (k, v) => (ys contains k) && (ys(k) isEqual v)
				})
	}

	override def isSubMap(other: IMap) = other match {
		case TypelessMap(ys) => xs.keys forall (k => (ys contains k) && (ys(k) isEqual xs(k)))
	}

	override def iterator = xs.keys iterator

	override def valueIterator = xs.values iterator

	@deprecated
	override def entryIterator: java.util.Iterator[java.util.Map.Entry[IValue, IValue]] = mapAsJavaMap(xs).entrySet iterator

	override def accept[T, E <: Throwable](v: IValueVisitor[T, E]): T = v visitMap this

	override def equals(that: Any): Boolean = that match {
		case other: TypelessMap => this.xs equals other.xs
		case _ => false
	}

	override def hashCode = xs.hashCode

}

object TypelessMap {
	type Coll = collection.immutable.HashMap[IValue, IValue]
	val empty = collection.immutable.HashMap.empty[IValue, IValue]
}
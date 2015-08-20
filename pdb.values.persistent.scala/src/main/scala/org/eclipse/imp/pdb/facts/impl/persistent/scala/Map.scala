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

case class Map(kt: Type, vt: Type, xs: scala.collection.immutable.Map[IValue, IValue])
	extends Value with IMap {

	override lazy val t = TypeFactory.getInstance mapType(kt, vt)

	override def isEmpty = xs isEmpty

	override def size = xs size

	override def put(k: IValue, v: IValue) = Map(this.kt lub k.getType, this.vt lub v.getType, xs + (k -> v))

	override def removeKey(k: IValue) = Map(this.kt, this.vt, xs - k)

	override def get(k: IValue) = xs getOrElse(k, null)

	override def containsKey(k: IValue) = xs contains k

	override def containsValue(v: IValue) = xs exists {
		case (_, cv) => v == cv
	}

	override def getKeyType = kt

	override def getValueType = vt

	override def join(other: IMap): IMap = other match {
		case Map(okt, ovt, ys) =>
			Map(this.kt lub okt, this.vt lub ovt, xs ++ ys)
	}

	override def remove(other: IMap): IMap = other match {
		case Map(okt, ovt, ys) =>
			Map(this.kt lub okt, this.vt lub ovt,
				xs -- ys.keySet)
	}

	override def compose(other: IMap): IMap = other match {
		case Map(_, ovt, ys) => Map(kt, ovt, for ((k, v) <- xs if ys contains v) yield (k, ys(v)))
	}

	override def common(other: IMap) = other match {
		case Map(okt, ovt, ys) =>
			Map(this.kt lub okt, this.vt lub ovt,
				xs filter {
					case (k, v) => (ys contains k) && (ys(k) isEqual v)
				})
	}

	override def isSubMap(other: IMap) = other match {
		case Map(_, _, ys) => xs.keys forall (k => (ys contains k) && (ys(k) isEqual xs(k)))
	}

	override def iterator = xs.keys iterator

	override def valueIterator = xs.values iterator

	@deprecated
	override def entryIterator: java.util.Iterator[java.util.Map.Entry[IValue, IValue]] = mapAsJavaMap(xs).entrySet iterator

	override def accept[T, E <: Throwable](v: IValueVisitor[T, E]): T = v visitMap this

	override def equals(that: Any): Boolean = that match {
		case other: Map => this.xs equals other.xs
		case _ => false
	}

	override lazy val hashCode = xs.hashCode

}

object Map {
	type Coll = collection.immutable.HashMap[IValue, IValue]
	val empty = collection.immutable.HashMap.empty[IValue, IValue]
}
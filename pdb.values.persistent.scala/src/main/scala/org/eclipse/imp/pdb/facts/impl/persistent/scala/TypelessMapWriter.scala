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

import org.eclipse.imp.pdb.facts.IMapWriter
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.IMap
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import org.eclipse.imp.pdb.facts.ITuple
import collection.immutable.Map.empty
import collection.JavaConversions.mapAsScalaMap
import collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.MapBuilder

sealed class TypelessMapWriter extends IMapWriter {
	
	val xs: MapBuilder[IValue, IValue, TypelessMap.Coll] = new MapBuilder(TypelessMap.empty)

	override def put(k: IValue, v: IValue) = xs += (k -> v)

	override def putAll(other: IMap) = other match {
		case Map(_, _, ys) => xs ++= ys
	}

	override def putAll(ys: java.util.Map[IValue, IValue]) = xs ++= ys

	override def insert(ys: IValue*): Unit = xs ++= (for (y <- ys; z = y.asInstanceOf[ITuple]) yield z.get(0) -> z.get(1))

	override def insertAll(ys: java.lang.Iterable[_ <: IValue]): Unit = ys foreach (this insert _)

	override def done = TypelessMap(xs.result)

}

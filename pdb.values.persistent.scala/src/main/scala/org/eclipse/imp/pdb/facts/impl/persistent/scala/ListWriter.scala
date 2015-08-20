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
import org.eclipse.imp.pdb.facts.IListWriter
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`._
import org.eclipse.imp.pdb.facts.`type`.TypeFactory

import collection.mutable.ListBuffer
import collection.JavaConversions.iterableAsScalaIterable

sealed class ListWriter extends IListWriter {

	val xs: ListBuffer[IValue] = ListBuffer[IValue]()

	override def insert(ys: IValue*): Unit = ys ++=: xs

	override def insert(ys: Array[IValue], i: Int, n: Int) = this insert ((ys slice(i, i + n)): _*)

	override def insertAll(ys: java.lang.Iterable[_ <: org.eclipse.imp.pdb.facts.IValue]) = xs prependAll ys

	override def insertAt(i: Int, ys: IValue*) = xs insertAll(i, ys)

	override def insertAt(i: Int, ys: Array[IValue], j: Int, n: Int) = this insertAt(i, (ys slice(j, j + n)): _*)

	override def replaceAt(i: Int, x: IValue) = xs update(i, x)

	override def append(ys: IValue*): Unit = xs ++= ys

	override def appendAll(ys: java.lang.Iterable[_ <: org.eclipse.imp.pdb.facts.IValue]) = xs appendAll ys

	override def done: IList = {
		val res = emptyList ++ xs.result
		List(`type` lub res, res)
	}

}

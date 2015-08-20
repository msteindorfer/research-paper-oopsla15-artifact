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

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.SetBuilder

import org.eclipse.imp.pdb.facts.ISet
import org.eclipse.imp.pdb.facts.ISetWriter
import org.eclipse.imp.pdb.facts.IValue

sealed class TypelessSetWriter extends ISetWriter {

	val xs: SetBuilder[IValue, Set.Coll] = new SetBuilder(Set.empty)

	override def insert(ys: IValue*) {
		xs ++= ys
	}

	override def insertAll(ys: java.lang.Iterable[_ <: IValue]) {
		xs ++= ys
	}

	override def done: ISet = { 
		TypelessSet(xs.result)
	}

}

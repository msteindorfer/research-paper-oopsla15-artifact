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

import scala.collection.JavaConversions.mapAsScalaMap

import org.eclipse.imp.pdb.facts.IConstructor
import org.eclipse.imp.pdb.facts.IList
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.Type

class ValueFactory extends org.eclipse.imp.pdb.facts.impl.primitive.AbstractPrimitiveValueFactory {

	def tuple = Tuple()

	def tuple(xs: IValue*) = Tuple(xs: _*)

	// TODO: currently the type is ignored and recalculated inside the constructor
	def tuple(t: Type, xs: IValue*) = Tuple(xs: _*)

	def node(name: String) = Node(name)

	def node(name: String, children: IValue*) = Node(name, Node.emptyChildren ++ children)

	def node(name: String, children: Array[IValue], keyArgValues: java.util.Map[String, IValue]) = ???

	def node(name: String, annotations: java.util.Map[String, IValue], children: IValue*) = Node(name, Node.emptyChildren ++ children).asAnnotatable().setAnnotations(annotations)

	def constructor(t: Type) = Constructor(t)

	def constructor(t: Type, children: IValue*) = Constructor(t, Constructor.emptyChildren ++ children)

	def constructor(t: Type, annotations: java.util.Map[String, IValue], children: IValue*): IConstructor = Constructor(t, Constructor.emptyChildren ++ children).asAnnotatable().setAnnotations(annotations)

	def constructor(t: Type, children: Array[IValue], kwParams: java.util.Map[String, IValue]): IConstructor = ???
	
	def set(t: Type) = setWriter(t).done

	def set(xs: IValue*) = {
		val writer = setWriter
		writer.insert(xs: _*)
		writer.done
	}

	def setWriter = new SetWriter

	def setWriter(t: Type) = setWriter

	def list(t: Type) = listWriter(t).done

	def list(xs: IValue*): IList = {
		val writer = listWriter
		writer.insert(xs: _*)
		writer.done
	}

	def listWriter = new ListWriter

	def listWriter(t: Type) = listWriter

	def relation(t: Type) = setWriter(t).done

	// TODO: add tests, not yet covered
	def relation(xs: IValue*) = set(xs: _*)

	def relationWriter(et: Type) = {
	  	require(et isTuple); 
	  	setWriter
	}

	def relationWriter = setWriter
	
	def map(kt: Type, vt: Type) = mapWriter(kt, vt).done

	def mapWriter = new MapWriter

	def mapWriter(kt: Type, vt: Type) = mapWriter

	def map(mapType: Type) = mapWriter(mapType).done

	def mapWriter(mapType: Type) = mapWriter

	def listRelation(t: Type) = listRelationWriter(t).done

	// TODO: add tests, not yet covered
	def listRelation(xs: IValue*) = list(xs: _*)

	def listRelationWriter = listWriter

	def listRelationWriter(et: Type) = {
		require(et isTuple)
		listWriter
	}

	override def toString = "VF_SCALA"

}

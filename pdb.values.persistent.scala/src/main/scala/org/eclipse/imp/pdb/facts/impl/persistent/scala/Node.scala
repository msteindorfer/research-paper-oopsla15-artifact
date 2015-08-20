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

import org.eclipse.imp.pdb.facts.INode
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import collection.JavaConversions.asJavaIterator
import collection.JavaConversions.mapAsJavaMap
import collection.JavaConversions.mapAsScalaMap
import org.eclipse.imp.pdb.facts.IList
import org.eclipse.imp.pdb.facts.impl.func.NodeFunctions
import org.eclipse.imp.pdb.facts.IAnnotatable
import org.eclipse.imp.pdb.facts.impl.AbstractDefaultAnnotatable
import org.eclipse.imp.pdb.facts.impl.AnnotatedNodeFacade
import org.eclipse.imp.pdb.facts.impl.AbstractNode

case class Node(val name: String, val children: Node.ChildrenColl) extends AbstractNode {

	def getType = TypeFactory.getInstance.nodeType

	def set(i: Int, x: IValue) = Node(name, children updated(i, x))

	def get(i: Int) = children(i)

	def arity = children.length

	def getName = name

	def getChildren = this

	def iterator = children.iterator

	def getKeywordArgumentNames: Array[String] = null // = ???
	
	def getValueFactory = new ValueFactory
	
	def isEqual(that: IValue): Boolean = NodeFunctions.isEqual(getValueFactory, this, that)
	
}

object Node {

	type ChildrenColl = collection.immutable.List[IValue]
	val emptyChildren = collection.immutable.List.empty[IValue]

	def apply(name: String): Node = apply(name, Node.emptyChildren)
	
}
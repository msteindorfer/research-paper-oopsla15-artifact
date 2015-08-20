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

import java.io.StringWriter
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.io.StandardTextWriter
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.IAnnotatable
import org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException

trait Value extends IValue {

	def t: Type

	def getType = t

	def isEqual(that: IValue) = this equals that

	def isAnnotatable = false
	
	def asAnnotatable: IAnnotatable[_ <: IValue] = {
		throw new IllegalOperationException(
				"Cannot be viewed as annotatable.", getType());
	}

	def mayHaveKeywordParameters(): Boolean = false

	def asWithKeywordParameters(): org.eclipse.imp.pdb.facts.IWithKeywordParameters[_ <: org.eclipse.imp.pdb.facts.IValue] = {
		throw new IllegalOperationException(
				"Cannot be viewed as annotatable.", getType());
	}
	
	final override def toString: String = {
		val stream = new StringWriter
		new StandardTextWriter write(this, stream)
		return stream.toString
	}

}

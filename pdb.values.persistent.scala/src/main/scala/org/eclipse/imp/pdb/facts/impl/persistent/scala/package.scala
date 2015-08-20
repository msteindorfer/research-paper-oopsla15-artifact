/*******************************************************************************
 * Copyright (c) 2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *    * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 ******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent

import org.eclipse.imp.pdb.facts.IValue

package object scala {

	type ListColl = collection.immutable.Vector[IValue]
	val emptyList: ListColl = collection.immutable.Vector.empty[IValue]

}

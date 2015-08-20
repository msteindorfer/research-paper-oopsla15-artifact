/*******************************************************************************
 * Copyright (c) 2012-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.persistent.clojure;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

class TypeInferenceHelper {

	protected static Type lubFromVoid(IValue[] xs) {
		return lub(xs, TypeFactory.getInstance().voidType());
	}
	
	protected static Type lub(IValue[] xs, Type base) {
		Type result = base;

		for (IValue x : xs) {
			result = result.lub(x.getType());
		}

		return result;
	}		
	
//	public static Type lub(IValue x, Type base) {
//		return base.lub(x.getType());
//	}
//
//	public static Type lub(IList xs, Type base) {
//		return base.lub(xs.getElementType());
//	}		
//
//	public static Type lub(ISet xs, Type base) {
//		return base.lub(xs.getElementType());
//	}
	
}

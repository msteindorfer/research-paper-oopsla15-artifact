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
package org.eclipse.imp.pdb.facts.impl.persistent.scala

import org.eclipse.imp.pdb.facts.ISet
import org.eclipse.imp.pdb.facts.ISetRelation
import org.eclipse.imp.pdb.facts.ITuple
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import scala.annotation.tailrec

object ImplicitRelationViewOnSet {

	implicit class RelationViewOnSet(rel1: ISet) extends ISetRelation[ISet] {

		require(rel1.getType.isRelation)

		def compose(rel2: ISetRelation[ISet]) = rel1 match {
			case rel1: Set => rel2.asSet() match {
				case rel2: Set => {
					val relationType = rel1.getType compose rel2.getType
					val tupleType = TypeFactory.getInstance.tupleType(rel1.getElementType().getFieldType(0), rel2.getElementType().getFieldType(1))

					val otherIndexed = rel2.xs groupBy {
						_.asInstanceOf[ITuple].get(0)
					}

					//      val tuples: Set.Coll = for {
					//        Tuple(x, y) <- this.ts
					//        Tuple(_, z) <- otherIndexed.getOrElse(y, Set.empty)
					//      } yield Tuple(tupleType, x, z)

					val tuples: Set.Coll = rel1.xs.flatMap {
						t1 =>
							otherIndexed.getOrElse(t1.asInstanceOf[ITuple].get(1), Set.empty).map {
								t2 =>
									Tuple(tupleType, t1.asInstanceOf[ITuple].get(0), t2.asInstanceOf[ITuple].get(1)): IValue
							}(scala.collection.breakOut)
					}

					Set(relationType.getFieldTypes, tuples)
				}
			}
		}

		def closure: ISet = {
			@tailrec def calculate(oldSize: Int, r: ISet): ISet = {
				if (r.size == oldSize) r
				else calculate(r.size, r union (r compose r))
			}

			calculate(0, rel1)
		}

		def closureStar: ISet = {
			val resultElementType = rel1.getType.closure.getElementType
			val reflex = Set(resultElementType, (for (x <- carrier.asInstanceOf[Set].xs) yield Tuple(resultElementType, x, x)))

			closure union reflex
		}

		def carrier: ISet = rel1 match {
			case Set(et, xs) =>
				val newElementType = rel1.getType.carrier.getElementType
				val newElementData = xs flatMap {
					_.asInstanceOf[Tuple].xs
				}

				Set(newElementType, newElementData)
		}

		def domain = valuesAtIndex(0)

		def range = valuesAtIndex(rel1.getType.getArity - 1)

		def valuesAtIndex(i: Int) = rel1 match {
			case Set(et, xs) =>
				Set(rel1.getType.getFieldType(i), for (x <- xs) yield x.asInstanceOf[ITuple].get(i))
		}

		def arity: Int = rel1.getElementType.getArity

		def project(fields: Int*): ISet = rel1 match {
			case Set(_, xs) =>

				val et = rel1.getType.getFieldTypes.select(fields: _*)
				val ys = (for (x <- xs) yield x.asInstanceOf[ITuple] select (fields: _*))

				Set(et, ys)
		}

		def projectByFieldNames(fields: String*): ISet =
			project((for (s <- fields) yield (rel1.getType.getFieldTypes getFieldIndex s)): _*)

		def asSet: ISet = rel1

	}

}

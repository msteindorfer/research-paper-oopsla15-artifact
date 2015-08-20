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

import org.eclipse.imp.pdb.facts.IList
import org.eclipse.imp.pdb.facts.IListRelation
import org.eclipse.imp.pdb.facts.`type`.TypeFactory
import org.eclipse.imp.pdb.facts.ITuple
import org.eclipse.imp.pdb.facts.IValue
import org.eclipse.imp.pdb.facts.`type`.Type
import org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException
import org.eclipse.imp.pdb.facts.impl.util.collections.ShareableValuesHashSet

object ImplicitRelationViewOnList {

	implicit class RelationViewOnList(rel1: IList) extends IListRelation[IList] {

		require(rel1.getType.isListRelation)

		//    def compose(rel2: IListRelation[IList]) = rel1 match {
		//      case rel1: List => rel2.asList() match {
		//        case rel2: List => {
		//          val relationType = rel1.getType compose rel2.getType
		//          val tupleType = TypeFactory.getInstance.tupleType(rel1.getElementType().getFieldType(0), rel2.getElementType().getFieldType(1))
		//
		//          val otherIndexed = rel2.xs groupBy { _.asInstanceOf[ITuple].get(0) }
		//
		//          //      val tuples: Set.Coll = for {
		//          //        Tuple(x, y) <- this.ts
		//          //        Tuple(_, z) <- otherIndexed.getOrElse(y, Set.empty)
		//          //      } yield Tuple(tupleType, x, z)
		//
		//          val tuples: ListColl = rel1.xs.flatMap { t1 =>
		//            otherIndexed.getOrElse(t1.asInstanceOf[ITuple].get(1), Set.empty).map { t2 =>
		//              Tuple(tupleType, t1.asInstanceOf[ITuple].get(0), t2.asInstanceOf[ITuple].get(1)): IValue
		//            }(scala.collection.breakOut)
		//          }
		//
		//          List(relationType.getFieldTypes, tuples)
		//        }
		//      }
		//    }

		//    	public static IList closure(IList rel1) {
		//		Type resultType = rel1.getType().closure(); // will throw exception if not binary and reflexive
		//		IList tmp = rel1;
		//
		//		int prevCount = 0;
		//
		//		ShareableValuesHashSet addedTuples = new ShareableValuesHashSet();
		//		while (prevCount != tmp.length()) {
		//			prevCount = tmp.length();
		//			IList tcomp = compose(tmp, tmp);
		//			IListWriter w = List.createListWriter(resultType.getElementType());
		//			for(IValue t1 : tcomp){
		//				if(!tmp.contains(t1)){
		//					if(!addedTuples.contains(t1)){
		//						addedTuples.add(t1);
		//						w.append(t1);
		//					}
		//				}
		//			}
		//			tmp = tmp.concat(w.done());
		//			addedTuples.clear();
		//		}
		//		return tmp;
		//	}

		//    def closure: IList = {
		//      @tailrec def calculate(oldSize: Int, r: IList): IList = {
		//        if (r.length == oldSize) r
		//        else calculate(r.length, r concat (r compose r))
		//      }
		//
		//      calculate(0, rel1)
		//    }

		private def f_compose(rel1: IList, rel2: IList): IList = {

			import collection.JavaConverters._

			val typeFactory = TypeFactory.getInstance();
			val voidType = typeFactory.voidType();
			val otherTupleType = rel2.getType().getFieldTypes();

			if (rel1.getElementType() == voidType) return rel1;
			if (otherTupleType == voidType) return rel2;

			if (rel1.getElementType().getArity() != 2 || otherTupleType.getArity() != 2) throw new IllegalOperationException("compose", rel1.getElementType(), otherTupleType);

			// Relaxed type constraint:
			if (!rel1.getElementType().getFieldType(1).comparable(otherTupleType.getFieldType(0))) throw new IllegalOperationException("compose", rel1.getElementType(), otherTupleType);

			val newTupleFieldTypes = Array[Type](rel1.getElementType().getFieldType(0), otherTupleType.getFieldType(1));
			val tupleType = typeFactory.tupleType(newTupleFieldTypes: _*);

			val w = new ListWriter;

			for (v1 <- rel1.asScala) {
				val tuple1 = v1.asInstanceOf[ITuple];
				for (v2 <- rel2.asScala) {
					val tuple2 = v2.asInstanceOf[ITuple];

					if (tuple1.get(1).isEqual(tuple2.get(0))) {
						w.append(Tuple(tuple1.get(0), tuple2.get(1)));
					}
				}
			}

			return w.done();
		}

		private def f_closure(rel1: IList): IList = {

			import collection.JavaConverters._

			val resultType = rel1.getType().closure();
			// will throw exception if not binary and reflexive
			var tmp = rel1;
			var prevCount = 0;

			val addedTuples = new ShareableValuesHashSet();
			while (prevCount != tmp.length()) {
				prevCount = tmp.length();
				val tcomp = f_compose(tmp, tmp);
				val w = new ListWriter
				for (t1 <- tcomp.asScala) {
					if (!tmp.contains(t1)) {
						if (!addedTuples.contains(t1)) {
							addedTuples.add(t1);
							w.append(t1);
						}
					}
				}
				tmp = tmp.concat(w.done());
				addedTuples.clear();
			}
			return tmp;
		}

		def compose(rel2: IListRelation[IList]) = f_compose(rel1, rel2.asList())

		def closure: IList = f_closure(rel1)

		def closureStar: IList = {
			val resultElementType = rel1.getType.closure.getElementType
			val reflex = List(resultElementType, (for (x <- carrier.asInstanceOf[List].xs) yield Tuple(resultElementType, x, x)))

			closure concat reflex
		}

		def carrier: IList = {
			import collection.JavaConverters._

			val newType = rel1.getType().carrier();
			val w = new ListWriter

			val cache = new java.util.HashSet[IValue]();

			for (v: IValue <- rel1.asScala) {
				val t = v.asInstanceOf[ITuple];
				for (e: IValue <- t.asScala) {
					if (!cache.contains(e)) {
						cache.add(e);
						w.append(e);
					}
				}
			}

			return w.done();
		}

		def domain = valuesAtIndex(0)

		def range = valuesAtIndex(rel1.getType.getArity - 1)

		def valuesAtIndex(i: Int) = rel1 match {
			case List(et, xs) =>
				List(rel1.getType.getFieldType(i), for (x <- xs) yield x.asInstanceOf[ITuple].get(i))
		}

		def arity: Int = rel1.getElementType.getArity

		def project(fields: Int*): IList = rel1 match {
			case List(_, xs) =>

				val et = rel1.getType.getFieldTypes.select(fields: _*)
				val ys = (for (x <- xs) yield x.asInstanceOf[ITuple] select (fields: _*))

				List(et, ys)
		}

		def projectByFieldNames(fields: String*): IList =
			project((for (s <- fields) yield (rel1.getType.getFieldTypes getFieldIndex s)): _*)

		def asList: IList = rel1

	}

	//    	public static IList closure(IList rel1) {
	//		Type resultType = rel1.getType().closure(); // will throw exception if not binary and reflexive
	//		IList tmp = rel1;
	//
	//		int prevCount = 0;
	//
	//		ShareableValuesHashSet addedTuples = new ShareableValuesHashSet();
	//		while (prevCount != tmp.length()) {
	//			prevCount = tmp.length();
	//			IList tcomp = compose(tmp, tmp);
	//			IListWriter w = List.createListWriter(resultType.getElementType());
	//			for(IValue t1 : tcomp){
	//				if(!tmp.contains(t1)){
	//					if(!addedTuples.contains(t1)){
	//						addedTuples.add(t1);
	//						w.append(t1);
	//					}
	//				}
	//			}
	//			tmp = tmp.concat(w.done());
	//			addedTuples.clear();
	//		}
	//		return tmp;
	//	}


	//  def closure(rel1: List): PartialFunction[List, List] = {
	//    case List(et, xs) if rel1.getElementType().getArity() == 2 => {
	//
	//      import collection.JavaConversions.iterableAsScalaIterable
	//
	//      val resultType = et.closure // will throw exception if not binary and reflexive
	//
	//      var tmp = rel1
	//      var prevCount = 0
	//
	//      val addedTuples = collection.mutable.Set.empty[IValue]
	//
	//      while (prevCount != tmp.length) {
	//        val tcomp = compose(tmp, tmp)
	//
	//        for (t1 <- tcomp) {
	//          if (!tmp.contains(t1) && !addedTuples.contains(t1)) {
	//        	  addedTuples.add(t1)
	//
	//          }
	//        }
	//      }
	//    }
	//  }

}

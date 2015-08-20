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



// TODO: make value class (i.e. extend from AnyVal)
//case class StringValue(val content: String) extends Value with IString {
//
//  override def t = TypeFactory.getInstance().stringType
//
//  def accept[T,E <: Throwable](v: IValueVisitor[T,E]): T = v visitString this
//
//  /**
//   * @return the Java string that this string represents
//   */
//  def getValue: String = content
//
//  /**
//   * Concatenates two strings
//   * @param other
//   * @return
//   */
//  def concat(other: IString): IString = other match {
//    case that: StringValue => new StringValue(this.content ++ that.content)
//  }
//
//  /**
//   * Reverses a string
//   */
//  def reverse: IString = new StringValue(new StringBuilder(content).reverse toString)
//
//  /**
//   * Computes the length of the string
//   * @return amount of Unicode characters
//   */
//  def length: Int = content.codePointCount(0, content.length)
//
//  /**
//   * Computes a substring
//   *
//   * @param start the inclusive start index
//   * @param end   the exclusive end index
//   */
//  def substring(start: Int, end: Int): IString = new StringValue(
//      content.substring(
//          content.offsetByCodePoints(0, start), 
//          content.offsetByCodePoints(0, end)))
//
//  /**
//   * Computes a substring
//   *
//   * @param start the inclusive start index
//   */
//  def substring(start: Int): IString = new StringValue(content.substring(start))
//
//  /**
//   * Compares two strings lexicographically
//   * @param other
//   * @return -1 if receiver is less than other, 0 is receiver is equal, 1 if receiver is larger
//   */
//  def compare(other: IString): Int = other match {
//    case that: StringValue => this.content compare that.content
//  }
//
//  /**
//   * Returns the Unicode character at the given index.
//   * @param index
//   * @return
//   */
//  def charAt(index: Int): Int = content.codePointAt(content.offsetByCodePoints(0, index))
//
//  def replace(first: Int, second: Int, end: Int, repl: IString): IString = ???
// 
//}

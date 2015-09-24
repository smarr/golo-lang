/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.runtime;

import gololang.truffle.ExpressionNode;
import gololang.truffle.NotYetImplemented;
import gololang.truffle.nodes.binary.BinaryNode;
import gololang.truffle.nodes.binary.LessThanNodeGen;
import gololang.truffle.nodes.binary.MinusNodeGen;
import gololang.truffle.nodes.binary.PlusNodeGen;


public enum OperatorType {

  PLUS("+") {
	 @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return PlusNodeGen.create(left, right);
    }
  },
  MINUS("-") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return MinusNodeGen.create(left, right);
    }
  },
  TIMES("*"),
  DIVIDE("/"),
  MODULO("%"),

  EQUALS("=="),
  NOTEQUALS("!="),
  LESS("<") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return LessThanNodeGen.create(left, right);
    }
  },
  LESSOREQUALS("<="),
  MORE(">"),
  MOREOREQUALS(">="),

  BIT_OR("bitOR"),
  BIT_XOR("bitXOR"),
  BIT_LSHIFT("bitLSHIFT"),

  AND("and"),
  OR("or"),
  NOT("not"),

  IS("is"),
  ISNT("isnt"),

  OFTYPE("oftype"),

  ORIFNULL("orIfNull"),

  ANON_CALL(""),
  METHOD_CALL(":"),
  ELVIS_METHOD_CALL("?:");

  private final String symbol;

  OperatorType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
  
  public ExpressionNode createNode(ExpressionNode left, ExpressionNode right) { throw new NotYetImplemented(); };
}

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
import gololang.truffle.nodes.binary.BitLeftShiftNodeGen;
import gololang.truffle.nodes.binary.BitOrNodeGen;
import gololang.truffle.nodes.binary.BitXorNodeGen;
import gololang.truffle.nodes.binary.DivideNodeGen;
import gololang.truffle.nodes.binary.EqualNodeGen;
import gololang.truffle.nodes.binary.GreaterThanNodeGen;
import gololang.truffle.nodes.binary.LessThanNodeGen;
import gololang.truffle.nodes.binary.MinusNodeGen;
import gololang.truffle.nodes.binary.NotEqualNodeGen;
import gololang.truffle.nodes.binary.PlusNodeGen;
import gololang.truffle.nodes.binary.TimesNodeGen;
import gololang.truffle.nodes.unary.UnaryNode;
import gololang.truffle.nodes.unary.NotNodeGen;


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
  TIMES("*") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return TimesNodeGen.create(left, right);
    }
  },
  DIVIDE("/") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return DivideNodeGen.create(left, right);
    }
  },
  MODULO("%"),

  EQUALS("==") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return EqualNodeGen.create(left, right);
    }
  },
  NOTEQUALS("!=") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return NotEqualNodeGen.create(left, right);
    }
  },
  LESS("<") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return LessThanNodeGen.create(left, right);
    }
  },
  LESSOREQUALS("<="),
  MORE(">") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return GreaterThanNodeGen.create(left, right);
    }
  },
  MOREOREQUALS(">="),

  BIT_OR("bitOR") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return BitOrNodeGen.create(left, right);
    }
  },
  BIT_XOR("bitXOR") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return BitXorNodeGen.create(left, right);
    }
  },
  BIT_LSHIFT("bitLSHIFT") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return BitLeftShiftNodeGen.create(left, right);
    }
  },

  AND("and"),
  OR("or"),
  NOT("not") {
    @Override
    public UnaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return NotNodeGen.create(left);
    }
  },

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

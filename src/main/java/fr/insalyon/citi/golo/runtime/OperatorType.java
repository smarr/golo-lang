/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.runtime;

import gololang.truffle.BinaryNode;
import gololang.truffle.ExpressionNode;
import gololang.truffle.NotNodeGen;
import gololang.truffle.NotYetImplemented;
import gololang.truffle.UnaryNode;
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
  MODULO("%") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },

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
  LESSOREQUALS("<=") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },
  MORE(">") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return GreaterThanNodeGen.create(left, right);
    }
  },
  MOREOREQUALS(">=") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },

  AND("and") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },
  OR("or") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },
  NOT("not") {
    @Override
    public UnaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      return NotNodeGen.create(left);
    }
  },

  IS("is") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },
  ISNT("isnt") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },

  OFTYPE("oftype") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },

  ORIFNULL("orIfNull") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },

  ANON_CALL("") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },
  METHOD_CALL(":") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  },
  ELVIS_METHOD_CALL("?:") {
    @Override
    public BinaryNode createNode(final ExpressionNode left, final ExpressionNode right) {
      throw new NotYetImplemented();
    }
  };

  private final String symbol;

  OperatorType(final String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }

  public abstract ExpressionNode createNode(ExpressionNode left, ExpressionNode right);
}

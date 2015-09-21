/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

import com.oracle.truffle.api.nodes.Node;

import fr.insalyon.citi.golo.compiler.TruffleGenerationGoloIrVisitor;
import fr.insalyon.citi.golo.runtime.OperatorType;

public class BinaryOperation extends ExpressionStatement {

  private final OperatorType type;
  private final ExpressionStatement leftExpression;
  private final ExpressionStatement rightExpression;

  public BinaryOperation(OperatorType type, ExpressionStatement leftExpression, ExpressionStatement rightExpression) {
    super();
    this.type = type;
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public OperatorType getType() {
    return type;
  }

  public ExpressionStatement getLeftExpression() {
    return leftExpression;
  }

  public ExpressionStatement getRightExpression() {
    return rightExpression;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitBinaryOperation(this);
  }

  @Override
  public Node accept(final TruffleGenerationGoloIrVisitor visitor) {
    return visitor.visitBinaryOperation(this);
  }
}

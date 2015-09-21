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

public class ConstantStatement extends ExpressionStatement {

  private final Object value;

  public ConstantStatement(Object value) {
    super();
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitConstantStatement(this);
  }
  
  @Override
  public Node accept(final TruffleGenerationGoloIrVisitor visitor) {
    return visitor.visitConstantStatement(this);
  }
}

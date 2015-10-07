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

public class LoopBreakFlowStatement extends GoloStatement {

  public static enum Type {
    BREAK, CONTINUE
  }

  private final Type type;
  private LoopStatement enclosingLoop;

  private LoopBreakFlowStatement(Type type) {
    super();
    this.type = type;
  }

  public static LoopBreakFlowStatement newContinue() {
    return new LoopBreakFlowStatement(Type.CONTINUE);
  }

  public static LoopBreakFlowStatement newBreak() {
    return new LoopBreakFlowStatement(Type.BREAK);
  }

  public Type getType() {
    return type;
  }

  public LoopStatement getEnclosingLoop() {
    return enclosingLoop;
  }

  public void setEnclosingLoop(LoopStatement enclosingLoop) {
    this.enclosingLoop = enclosingLoop;
  }

  @Override
  public void accept(GoloIrVisitor visitor) {
    visitor.visitLoopBreakFlowStatement(this);
  }
  
  @Override
  public Node accept(final TruffleGenerationGoloIrVisitor visitor) {
    return visitor.visitLoopBreakFlowStatement(this);
  }
}

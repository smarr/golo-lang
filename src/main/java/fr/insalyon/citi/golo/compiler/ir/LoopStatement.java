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

package fr.insalyon.citi.golo.compiler.ir;

import com.oracle.truffle.api.nodes.Node;

import fr.insalyon.citi.golo.compiler.TruffleGenerationGoloIrVisitor;

public class LoopStatement extends GoloStatement {

  private final AssignmentStatement initStatement;
  private final ExpressionStatement conditionStatement;
  private final GoloStatement postStatement;
  private final Block block;

  public LoopStatement(final AssignmentStatement initStatement, final ExpressionStatement conditionStatement, final Block block, final GoloStatement postStatement) {
    super();
    this.initStatement = initStatement;
    this.conditionStatement = conditionStatement;
    this.postStatement = postStatement;
    this.block = block;
  }

  public boolean hasInitStatement() {
    return initStatement != null;
  }

  public AssignmentStatement getInitStatement() {
    return initStatement;
  }

  public ExpressionStatement getConditionStatement() {
    return conditionStatement;
  }

  public Block getBlock() {
    return block;
  }

  public GoloStatement getPostStatement() {
    return postStatement;
  }

  public boolean hasPostStatement() {
    return postStatement != null;
  }

  @Override
  public void accept(final GoloIrVisitor visitor) {
    visitor.visitLoopStatement(this);
  }

  @Override
  public Node accept(final TruffleGenerationGoloIrVisitor visitor) {
    return visitor.visitLoopStatement(this);
  }
}

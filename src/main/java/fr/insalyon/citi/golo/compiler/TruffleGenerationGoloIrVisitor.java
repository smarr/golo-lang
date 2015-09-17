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

package fr.insalyon.citi.golo.compiler;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.oracle.truffle.api.frame.FrameDescriptor;

import fr.insalyon.citi.golo.compiler.ir.AbstractInvocation;
import fr.insalyon.citi.golo.compiler.ir.BinaryOperation;
import fr.insalyon.citi.golo.compiler.ir.Block;
import fr.insalyon.citi.golo.compiler.ir.ConditionalBranching;
import fr.insalyon.citi.golo.compiler.ir.ConstantStatement;
import fr.insalyon.citi.golo.compiler.ir.ExpressionStatement;
import fr.insalyon.citi.golo.compiler.ir.FunctionInvocation;
import fr.insalyon.citi.golo.compiler.ir.GoloFunction;
import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.ir.GoloStatement;
import fr.insalyon.citi.golo.compiler.ir.LocalReference;
import fr.insalyon.citi.golo.compiler.ir.ReferenceLookup;
import fr.insalyon.citi.golo.compiler.ir.ReferenceTable;
import fr.insalyon.citi.golo.compiler.ir.ReturnStatement;
import fr.insalyon.citi.golo.runtime.OperatorType;
import gololang.truffle.EvalArgumentsNode;
import gololang.truffle.ExpressionNode;
import gololang.truffle.Function;
import gololang.truffle.LiteralNode;
import gololang.truffle.LiteralNode.IntegerLiteralNode;
import gololang.truffle.LocalArgumentReadNode;
import gololang.truffle.NotYetImplemented;
import gololang.truffle.nodes.binary.BinaryNode;
import gololang.truffle.nodes.controlflow.FunctionInvocationNode;
import gololang.truffle.nodes.controlflow.IfNode;
import gololang.truffle.nodes.controlflow.ReturnNode;
import gololang.truffle.nodes.controlflow.SequenceNode;
import gololang.truffle.LiteralNode.NullLiteralNode;


public class TruffleGenerationGoloIrVisitor {

  private Context context;
  private GoloModule module;

  private static class Context {
    private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    private final Deque<FrameDescriptor> frameDescriptors   = new LinkedList<>();
  }

  public void generateRepresentation(final GoloModule module) {
    this.module  = module;
    this.context = new Context();

    module.accept(this);
  }

  public void visitModule(final GoloModule module) {
	  // TODO: load imports
	  for (GoloFunction function : module.getFunctions()) {
		  module.addFunction(function.accept(this));
	  }
  }

  public Function visitFunction(final GoloFunction function) {
    FrameDescriptor frameDesc = new FrameDescriptor(null);
    context.frameDescriptors.push(frameDesc);

    if (function.isDecorated()) { NotYetImplemented.t(); }

    ExpressionNode body = function.getBlock().accept(this);
    context.frameDescriptors.pop();
    return new Function(body, function, frameDesc);
  }

  public ExpressionNode visitBlock(final Block block) {
    ReferenceTable referenceTable = block.getReferenceTable();
    context.referenceTableStack.push(referenceTable);

    List<GoloStatement> irStatements = block.getStatements();
    List<ExpressionNode> statements = new ArrayList<ExpressionNode>(irStatements.size());
    for (GoloStatement statement : irStatements) {
      statements.add((ExpressionNode) statement.accept(this));
    }

    context.referenceTableStack.pop();

    return createSequence(statements);
  }

  private ExpressionNode createSequence(final List<ExpressionNode> statements) {
    if (statements.size() == 0) {
      throw new NotYetImplemented();
    }
    if (statements.size() == 1) {
      return statements.get(0);
    }
    return new SequenceNode(statements.toArray(new ExpressionNode[statements.size()]));
  }

  private boolean isMethodCall(final BinaryOperation operation) {
    return operation.getType() == OperatorType.METHOD_CALL
            || operation.getType() == OperatorType.ELVIS_METHOD_CALL
            || operation.getType() == OperatorType.ANON_CALL;
  }

  public LiteralNode visitConstantStatement(final ConstantStatement constantStatement) {
    Object value = constantStatement.getValue();
    if (value == null) {
      return new NullLiteralNode();
    }
    if (value instanceof Integer) {
      return new IntegerLiteralNode((Integer) value);
    }
    throw new NotYetImplemented();
  }

  public ExpressionNode visitReturnStatement(final ReturnStatement returnStatement) {
    // TODO: ok, if we are at the end of a function, we don't want a return exception
    //       only and only if we are really in some nested expression, we need to throw an exception
    //       otherwise, we can always just return here the expression statement directly
    return new ReturnNode((ExpressionNode) returnStatement.getExpressionStatement().accept(this));
  }

  private ExpressionNode[] visitInvocationArguments(final AbstractInvocation invocation) {
    List<ExpressionStatement> argStatements = invocation.getArguments();
    ExpressionNode[] argumentNodes = new ExpressionNode[argStatements.size()];
    int i = 0;
    for (ExpressionStatement argument : argStatements) {
      if (invocation.usesNamedArguments()) {
        NotYetImplemented.t();
      }
      argumentNodes[i] = (ExpressionNode) argument.accept(this);
      i++;
    }
    return argumentNodes;
  }

  public ExpressionNode visitFunctionInvocation(final FunctionInvocation functionInvocation) {
    if (functionInvocation.isConstant() || functionInvocation.isOnReference() || functionInvocation.isOnModuleState() || functionInvocation.isAnonymous()) {
      throw new NotYetImplemented();
    }
    ExpressionNode[] arguments = visitInvocationArguments(functionInvocation);

    for (FunctionInvocation invocation : functionInvocation.getAnonymousFunctionInvocations()) {
      throw new NotYetImplemented();
    }

    // Could also be a ClosureInvocationNode, see one of the NYI branches. and earlier isConstant()
    return FunctionInvocationNode.create(functionInvocation.getName(), module, new EvalArgumentsNode(arguments));
  }

  public ExpressionNode visitReferenceLookup(final ReferenceLookup referenceLookup) {
    LocalReference reference = referenceLookup.resolveIn(context.referenceTableStack.peek());
    if (reference.isModuleState()) {
      throw new NotYetImplemented();
    } else if (reference.isArgument()) {
      return new LocalArgumentReadNode(reference.getIndex());
    } else {
      throw new NotYetImplemented();
    }
  }

  public ExpressionNode visitConditionalBranching(final ConditionalBranching conditionalBranching) {
    ExpressionNode condition = (ExpressionNode) conditionalBranching.getCondition().accept(this);
    ExpressionNode thenNode  = conditionalBranching.getTrueBlock().accept(this);
    ExpressionNode elseNode;

    if (conditionalBranching.hasFalseBlock()) {
      elseNode = conditionalBranching.getFalseBlock().accept(this);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      elseNode = (ExpressionNode) conditionalBranching.getElseConditionalBranching().accept(this);
    } else {
      elseNode = null;
    }
    return new IfNode(condition, thenNode, elseNode);
  }

  public BinaryNode visitBinaryOperation(final BinaryOperation binaryOperation) {
    OperatorType operatorType = binaryOperation.getType();
    if (!isMethodCall(binaryOperation)) {
      return (BinaryNode) operatorType.createNode(
          (ExpressionNode) binaryOperation.getLeftExpression().accept(this),
          (ExpressionNode) binaryOperation.getRightExpression().accept(this));
    } else {
      throw new NotYetImplemented();
    }
  }
}

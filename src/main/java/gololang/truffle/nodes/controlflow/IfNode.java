package gololang.truffle.nodes.controlflow;

import gololang.truffle.ExpressionNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;


public class IfNode extends ExpressionNode {

  @Child protected ExpressionNode condition;
  @Child protected ExpressionNode thenNode;
  @Child protected ExpressionNode elseNode;

  public IfNode(final ExpressionNode condition,
      final ExpressionNode thenNode, final ExpressionNode elseNode) {
    this.condition = condition;
    this.thenNode  = thenNode;
    this.elseNode  = elseNode;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    if (doCondition(frame)) {
      thenNode.executeGeneric(frame);
    } else if (elseNode != null) {
      elseNode.executeGeneric(frame);
    }
    return null;
  }

  private boolean doCondition(final VirtualFrame frame) {
    try {
      return condition.executeBoolean(frame);
    } catch (UnexpectedResultException e) {
      CompilerDirectives.transferToInterpreter();
      throw new UnsupportedSpecializationException(
          this, new Node[]{condition}, e.getResult());
    }
  }
}

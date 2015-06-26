package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;


public final class ReturnNode extends ExpressionNode {

  @Child protected ExpressionNode expr;

  public ReturnNode(final ExpressionNode expr) {
    this.expr = expr;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    throw new ReturnException(expr.executeGeneric(frame));
  }
}

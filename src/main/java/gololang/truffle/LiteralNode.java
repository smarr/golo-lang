package gololang.truffle;

import gololang.truffle.ExpressionNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class LiteralNode extends ExpressionNode {

  public static final class NullLiteralNode extends LiteralNode {
    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return null;
    }
  }

  public static final class IntegerLiteralNode extends LiteralNode {
    private final int val;
    public IntegerLiteralNode(final int val) {
      this.val = val;
    }

    @Override
    public int executeInteger(final VirtualFrame frame) {
      return val;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return val;
    }
  }
}

package gololang.truffle.literals;

import gololang.truffle.ExpressionNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class LiteralNode extends ExpressionNode {

  public static final class TrueLiteralNode extends LiteralNode {
    @Override
    public boolean executeBoolean(final VirtualFrame frame) {
      return true;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return true;
    }
  }

  public static final class FalseLiteralNode extends LiteralNode {
    @Override
    public boolean executeBoolean(final VirtualFrame frame) {
      return false;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return false;
    }
  }
}

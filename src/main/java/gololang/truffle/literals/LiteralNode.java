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

  public static final class LongLiteralNode extends LiteralNode {
    private final long val;
    public LongLiteralNode(final long val) {
      this.val = val;
    }

    @Override
    public long executeLong(final VirtualFrame frame) {
      return val;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return val;
    }
  }

  public static final class StringLiteralNode extends LiteralNode {
    private final String val;
    public StringLiteralNode(final String val) {
      this.val = val;
    }

    @Override
    public String executeString(final VirtualFrame frame) {
      return val;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return val;
    }
  }

  public static final class CharacterLiteralNode extends LiteralNode {
    private final char val;
    public CharacterLiteralNode(final char val) {
      this.val = val;
    }

    @Override
    public char executeCharacter(final VirtualFrame frame) {
      return val;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return val;
    }
  }

  public static final class DoubleLiteralNode extends LiteralNode {
    private final double val;
    public DoubleLiteralNode(final double val) {
      this.val = val;
    }

    @Override
    public double executeDouble(final VirtualFrame frame) {
      return val;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return val;
    }
  }

  public static final class FloatLiteralNode extends LiteralNode {
    private final float val;
    public FloatLiteralNode(final float val) {
      this.val = val;
    }

    @Override
    public float executeFloat(final VirtualFrame frame) {
      return val;
    }

    @Override
    public Object executeGeneric(final VirtualFrame frame) {
      return val;
    }
  }
}

package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;


public final class LocalArgumentReadNode extends ExpressionNode {
  protected final int index;

  public LocalArgumentReadNode(final int index) {
    this.index = index;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    return frame.getArguments()[index];
  }
}

package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class LocalVariableReadNode extends ExpressionNode {

  protected final FrameSlot slot;

  public LocalVariableReadNode(final FrameSlot slot) {
    this.slot = slot;
  }

  @Specialization
  public Object doObject(final VirtualFrame frame) {
    return frame.getValue(slot);
  }
}

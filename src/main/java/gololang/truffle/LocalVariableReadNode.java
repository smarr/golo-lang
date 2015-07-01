package gololang.truffle;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;


public final class LocalVariableReadNode extends ExpressionNode {

  protected final FrameSlot slot;

  public LocalVariableReadNode(final FrameSlot slot) {
    this.slot = slot;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    // TODO: might need to change this here after specializing frame slot kinds
    assert slot.getKind() == FrameSlotKind.Object;
    return frame.getValue(slot);
  }
}

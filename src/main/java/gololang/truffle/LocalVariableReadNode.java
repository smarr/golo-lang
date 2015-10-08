package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class LocalVariableReadNode extends ExpressionNode {

  protected final FrameSlot slot;

  public LocalVariableReadNode(final FrameSlot slot) {
    this.slot = slot;
  }

  @Specialization(guards = "isUninitialized()")
  public Object doNull() {
    return null;
  }

  @Specialization(guards = "isInitialized()", rewriteOn = {FrameSlotTypeException.class})
  public Object doObject(final VirtualFrame frame) throws FrameSlotTypeException {
    return frame.getObject(slot);
  }

  protected boolean isInitialized() {
    return slot.getKind() != FrameSlotKind.Illegal;
  }

  protected boolean isUninitialized() {
    return slot.getKind() == FrameSlotKind.Illegal;
  }
}

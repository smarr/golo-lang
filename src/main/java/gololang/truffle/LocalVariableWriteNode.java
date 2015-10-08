package gololang.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;


@NodeChild(value = "expr", type = ExpressionNode.class)
public abstract class LocalVariableWriteNode extends ExpressionNode {

  protected final FrameSlot slot;

  public LocalVariableWriteNode(final FrameSlot slot) {
    this.slot = slot;
  }
  
  @Specialization
  public final Object writeGeneric(final VirtualFrame frame, final Object expValue) {
    ensureObjectKind();
    frame.setObject(slot, expValue);
    return expValue;
  }

  protected final void ensureObjectKind() {
    if (slot.getKind() != FrameSlotKind.Object) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      slot.setKind(FrameSlotKind.Object);
    }
  }
}

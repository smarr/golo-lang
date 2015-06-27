package gololang.truffle;

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
  public Object doWrite(final VirtualFrame frame, final Object value) {
    assert slot.getKind() == FrameSlotKind.Object;
    frame.setObject(slot, value);
    return value;
  }
}

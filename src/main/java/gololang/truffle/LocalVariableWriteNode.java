package gololang.truffle;

import static com.oracle.truffle.api.CompilerDirectives.transferToInterpreter;

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

  @Specialization(guards = "isBoolKind()")
  public final boolean writeBoolean(final VirtualFrame frame, final boolean expValue) {
    frame.setBoolean(slot, expValue);
    return expValue;
  }

  @Specialization(guards = "isLongKind()")
  public final long writeLong(final VirtualFrame frame, final long expValue) {
    frame.setLong(slot, expValue);
    return expValue;
  }

  @Specialization(guards = "isDoubleKind()")
  public final double writeDouble(final VirtualFrame frame, final double expValue) {
    frame.setDouble(slot, expValue);
    return expValue;
  }

  @Specialization(contains = {"writeBoolean", "writeLong", "writeDouble"})
  public final Object writeGeneric(final VirtualFrame frame, final Object expValue) {
    ensureObjectKind();
    frame.setObject(slot, expValue);
    return expValue;
  }

  protected final boolean isBoolKind() {
    if (slot.getKind() == FrameSlotKind.Boolean) {
      return true;
    }
    if (slot.getKind() == FrameSlotKind.Illegal) {
      transferToInterpreter(); // "LocalVar.writeBoolToUninit"
      slot.setKind(FrameSlotKind.Boolean);
      return true;
    }
    return false;
  }

  protected final boolean isLongKind() {
    if (slot.getKind() == FrameSlotKind.Long) {
      return true;
    }
    if (slot.getKind() == FrameSlotKind.Illegal) {
      transferToInterpreter(); // "LocalVar.writeIntToUninit"
      slot.setKind(FrameSlotKind.Long);
      return true;
    }
    return false;
  }

  protected final boolean isDoubleKind() {
    if (slot.getKind() == FrameSlotKind.Double) {
      return true;
    }
    if (slot.getKind() == FrameSlotKind.Illegal) {
      transferToInterpreter(); // "LocalVar.writeDoubleToUninit"
      slot.setKind(FrameSlotKind.Double);
      return true;
    }
    return false;
  }

  protected final void ensureObjectKind() {
    if (slot.getKind() != FrameSlotKind.Object) {
      transferToInterpreter(); // "LocalVar.writeObjectToUninit"
      slot.setKind(FrameSlotKind.Object);
    }
  }
}

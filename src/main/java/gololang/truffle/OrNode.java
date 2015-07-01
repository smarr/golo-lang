package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public class OrNode {
  @Specialization
  public final boolean doOr(final VirtualFrame frame, final boolean left, final boolean right) {
    return left || right;
  }
}

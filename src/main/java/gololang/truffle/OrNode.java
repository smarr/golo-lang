package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public class OrNode {
	
  @Specialization
  public final boolean doOr(VirtualFrame frame, boolean left, boolean right) {
	return left || right;
  }
}

package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class IntValue extends UnaryNode implements PreEvaluated {

  public abstract Object executeEvaluated(VirtualFrame frame, Object value);

  @Override
  public final Object doEvaluated(final VirtualFrame frame, final Object[] args) {
    return executeEvaluated(frame, args[0]);
  }

  @Specialization
  public int doDouble(final double value) {
    return (int) value;
  }
}

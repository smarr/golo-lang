package gololang.truffle.nodes.unary;

import gololang.truffle.PreEvaluated;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class DoubleValue extends UnaryNode implements PreEvaluated {
  public abstract Object executeEvaluated(VirtualFrame frame, Object value);

  @Override
  public final Object doEvaluated(final VirtualFrame frame, final Object[] args) {
    return executeEvaluated(frame, args[0]);
  }

  @Specialization
  public double doInteger(final int value) {
    return value;
  }
}

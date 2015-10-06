package gololang.truffle.unary;

import gololang.truffle.PreEvaluated;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;


public abstract class PrintlnNode extends UnaryNode implements PreEvaluated {
  public abstract Object executeEvaluated(VirtualFrame frame, Object value);

  @Override
  public final Object doEvaluated(final VirtualFrame frame, final Object[] args) {
    return executeEvaluated(frame, args[0]);
  }

  @Specialization
  public Object printObject(final Object obj) {
    System.out.println(obj);
    return null;
  }
}

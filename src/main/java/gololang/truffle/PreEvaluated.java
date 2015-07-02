package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;


public interface PreEvaluated {
  Object doEvaluated(VirtualFrame frame, Object[] args);
}

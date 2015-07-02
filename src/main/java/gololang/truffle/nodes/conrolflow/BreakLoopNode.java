package gololang.truffle.nodes.conrolflow;

import gololang.truffle.ExpressionNode;

import com.oracle.truffle.api.frame.VirtualFrame;


public class BreakLoopNode extends ExpressionNode {

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    throw new BreakLoopException();
  }

}

package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;


public final class EvalArgumentsNode extends ExpressionNode {

  @Children protected final ExpressionNode[] argumentNodes;

  public EvalArgumentsNode(final ExpressionNode[] argumentNodes) {
    this.argumentNodes = argumentNodes;
  }

  @Override
  @ExplodeLoop
  public Object[] executeObjectArray(final VirtualFrame frame) {
    Object[] arguments = new Object[argumentNodes.length];
    for (int i = 0; i < argumentNodes.length; i++) {
      arguments[i] = argumentNodes[i].executeGeneric(frame);
    }
    return arguments;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    return executeObjectArray(frame);
  }
}

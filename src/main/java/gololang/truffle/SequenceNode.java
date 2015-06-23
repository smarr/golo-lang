package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;


public final class SequenceNode extends ExpressionNode {

  @Children private final ExpressionNode[] expressions;

  public SequenceNode(final ExpressionNode[] expressions) {
    this.expressions = expressions;
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    executeAllButLast(frame);
    return expressions[expressions.length - 1].executeGeneric(frame);
  }

  @ExplodeLoop
  private void executeAllButLast(final VirtualFrame frame) {
    for (int i = 0; i < expressions.length - 1; i++) {
      expressions[i].executeGeneric(frame);
    }
  }
}

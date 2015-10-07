package gololang.truffle.nodes.controlflow;

import gololang.truffle.ExpressionNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.UnexpectedResultException;


public final class ForLoopNode extends ExpressionNode {

  @Child protected LoopNode loopNode;
  @Child protected ExpressionNode init;

  public ForLoopNode(final ExpressionNode init,
      final ExpressionNode condition,
      final ExpressionNode body,
      final ExpressionNode post) {
    this.init = init;
    loopNode = Truffle.getRuntime().createLoopNode(
        new RepeatNode(condition, body, post));
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    if (init != null) {
      init.executeGeneric(frame);
    }
    try {
      loopNode.executeLoop(frame);
    } catch (BreakLoopException e) { /* just left the loop */ }
    return null;
  }

  public static final class RepeatNode extends Node implements RepeatingNode {

    @Child protected ExpressionNode condition;
    @Child protected ExpressionNode body;
    @Child protected ExpressionNode post;

    public RepeatNode(final ExpressionNode condition,
        final ExpressionNode body,
        final ExpressionNode post) {
      this.condition = condition;
      this.body = body;
      this.post = post;
    }

    @Override
    public boolean executeRepeating(final VirtualFrame frame) {
      if (shouldExecute(frame)) {
        body.executeGeneric(frame);
        if (post != null) {
          post.executeGeneric(frame);
        }
        return true;
      } else {
        return false;
      }
    }

    private boolean shouldExecute(final VirtualFrame frame) {
      try {
        return condition.executeBoolean(frame);
      } catch (UnexpectedResultException e) {
        CompilerDirectives.transferToInterpreter();
        throw new UnsupportedSpecializationException(
            this, new Node[]{condition}, e.getResult());
      }
    }
  }
}

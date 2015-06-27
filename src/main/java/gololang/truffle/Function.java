package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

import fr.insalyon.citi.golo.compiler.ir.GoloFunction;


public final class Function extends RootNode {
  @Child protected ExpressionNode expr;
  protected final GoloFunction   function;

  public Function(final ExpressionNode expr, final GoloFunction function) {
    this.expr = expr;
    this.function = function;
  }

  @Override
  public Object execute(final VirtualFrame frame) {
    return expr.executeGeneric(frame);
  }

  public GoloFunction getFunction() {
    return function;
  }
}
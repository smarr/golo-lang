package gololang.truffle;

import com.oracle.truffle.api.nodes.ControlFlowException;


public final class ReturnException extends ControlFlowException {

  private static final long serialVersionUID = 7613567555163509250L;

  private final Object result;

  public ReturnException(final Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}

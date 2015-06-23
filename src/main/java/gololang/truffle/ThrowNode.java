package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class ThrowNode extends UnaryNode {
  @Specialization
  public final Object doThrow(final Throwable throwable) {
    // TODO: this is not going to fly...
    //       what do we do here? do we wrap it in a runtime exception, and unwrap it on catch?
    throw throwable;
  }
}

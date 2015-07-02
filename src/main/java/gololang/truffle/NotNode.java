package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class NotNode extends UnaryNode {
  @Specialization
  public boolean doBoolean(final boolean value) {
    return !value;
  }
}

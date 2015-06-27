package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class GreaterThanNode extends BinaryNode {

  @Specialization
  public boolean doLongs(final long left, final long right) {
    return left > right;
  }
}
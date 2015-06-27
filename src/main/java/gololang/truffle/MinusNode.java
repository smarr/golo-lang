package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class MinusNode extends BinaryNode {

  @Specialization
  public long doLongs(final long left, final long right) {
    return left - right;
  }

}

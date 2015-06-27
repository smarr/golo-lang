package gololang.truffle;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class PlusNode extends BinaryNode {
  @Specialization
  public long doLongs(final long left, final long right) {
    return left + right;
  }
}

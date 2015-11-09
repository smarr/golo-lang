package gololang.truffle.nodes.binary;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class EqualNode extends BinaryNode {
  @Specialization
  public boolean doLongs(final long left, final long right) {
    return left == right;
  }

  @Specialization
  public boolean doIntegers(final int left, final int right) {
    return left == right;
  }
}
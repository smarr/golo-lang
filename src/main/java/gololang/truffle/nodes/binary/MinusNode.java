package gololang.truffle.nodes.binary;

import gololang.truffle.BinaryNode;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class MinusNode extends BinaryNode {

  @Specialization
  public long doLongs(final long left, final long right) {
    return left - right;
  }

  @Specialization
  public double doDoubles(final double left, final double right) {
    return left - right;
  }

  @Specialization
  public int doIntegers(final int left, final int right) {
    return left - right;
  }
}

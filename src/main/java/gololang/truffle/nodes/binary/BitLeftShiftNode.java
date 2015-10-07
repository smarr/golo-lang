package gololang.truffle.nodes.binary;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class BitLeftShiftNode extends BinaryNode {
  @Specialization
  public long doLongs(final long left, final long right) {
    return left << right;
  }

  @Specialization
  public int doIntegers(final int left, final int right) {
    return left << right;
  }
}

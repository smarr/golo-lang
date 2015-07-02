package gololang.truffle.nodes.binary;

import gololang.truffle.BinaryNode;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class GreaterThanNode extends BinaryNode {

  @Specialization
  public boolean doLongs(final long left, final long right) {
    return left > right;
  }

  @Specialization
  public boolean doIntegers(final int left, final int right) {
    return left > right;
  }

  @Specialization
  public boolean doDoubles(final double left, final double right) {
    return left > right;
  }
}

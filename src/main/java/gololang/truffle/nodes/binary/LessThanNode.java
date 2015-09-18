package gololang.truffle.nodes.binary;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class LessThanNode extends BinaryNode {

  @Specialization
  public boolean doIntegers(final int left, final int right) {
    return left < right;
  }
}

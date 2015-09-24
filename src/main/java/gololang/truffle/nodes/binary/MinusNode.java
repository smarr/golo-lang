package gololang.truffle.nodes.binary;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class MinusNode extends BinaryNode {

  @Specialization
  public int doIntegers(final int left, final int right) {
    return left - right;
  }
}

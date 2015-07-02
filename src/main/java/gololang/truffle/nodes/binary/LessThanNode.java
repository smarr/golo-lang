package gololang.truffle.nodes.binary;

import gololang.truffle.BinaryNode;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class LessThanNode extends BinaryNode {

  @Specialization
  public boolean doLongs(final long left, final long right) {
    return left < right;
  }
}

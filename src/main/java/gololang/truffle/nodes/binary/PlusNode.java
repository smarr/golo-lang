package gololang.truffle.nodes.binary;

import gololang.truffle.BinaryNode;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class PlusNode extends BinaryNode {
  @Specialization
  public long doLongs(final long left, final long right) {
    return left + right;
  }

  @Specialization
  public String doStrings(final String left, final String right) {
    return left + right;
  }

  @Specialization
  public String doLongAndString(final long left, final String right) {
    return left + right;
  }

  public String doStringAndLong(final String left, final long right) {
    return left + right;
  }
}

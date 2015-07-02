package gololang.truffle.nodes.binary;

import gololang.truffle.BinaryNode;

import com.oracle.truffle.api.dsl.Specialization;


public abstract class PlusNode extends BinaryNode {
  @Specialization
  public long doLongs(final long left, final long right) {
    return left + right;
  }

  @Specialization
  public int doIntegers(final int left, final int right) {
    return left + right;
  }

  @Specialization
  public String doStrings(final String left, final String right) {
    return left + right;
  }

  @Specialization
  public double doDoubles(final double left, final double right) {
    return left + right;
  }

  @Specialization
  public String doStringAndLong(final String left, final long right) {
    return left + right;
  }

  @Specialization
  public String doStringAndInteger(final String left, final int right) {
    return left + right;
  }

  @Specialization
  public String doStringAndDouble(final String left, final double right) {
    return left + right;
  }

  @Specialization
  public String doIntegerAndString(final int left, final String right) {
    return left + right;
  }

  @Specialization
  public String doLongAndString(final long left, final String right) {
    return left + right;
  }
}

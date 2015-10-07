package gololang.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;


@TypeSystem({
  int.class,
  boolean.class,
  long.class,
  double.class,
  String.class,
  Object[].class
})
public class Types { }

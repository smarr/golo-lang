package gololang.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;

import fr.insalyon.citi.golo.compiler.parser.GoloParser;

@TypeSystem({
  int.class,
  boolean.class,
  long.class,
  double.class,
  float.class,
  char.class,
  String.class,
  GoloParser.ParserClassRef.class,
  GoloParser.FunctionRef.class,
  Throwable.class
})
public class Types { }

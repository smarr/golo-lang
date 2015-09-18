package gololang.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;

import fr.insalyon.citi.golo.compiler.parser.GoloParser;


@TypeSystem({
  int.class,
  boolean.class,
  GoloParser.ParserClassRef.class,
  GoloParser.FunctionRef.class,
  Object[].class
})
public class Types { }

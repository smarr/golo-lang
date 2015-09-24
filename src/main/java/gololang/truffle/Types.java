package gololang.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;

import fr.insalyon.citi.golo.compiler.parser.GoloParser;


@TypeSystem({
  int.class,
  boolean.class,
  Object[].class
})
public class Types { }

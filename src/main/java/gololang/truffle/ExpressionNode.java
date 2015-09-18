package gololang.truffle;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

import fr.insalyon.citi.golo.compiler.parser.GoloParser.FunctionRef;
import fr.insalyon.citi.golo.compiler.parser.GoloParser.ParserClassRef;

public abstract class ExpressionNode extends Node {
  // TODO add constructor that takes SourceSection to simplify debugging

  public abstract Object executeGeneric(final VirtualFrame frame);

  public int executeInteger(final VirtualFrame frame) throws UnexpectedResultException {
	return TypesGen.expectInteger(executeGeneric(frame));
  }

  public boolean executeBoolean(final VirtualFrame frame) throws UnexpectedResultException {
	return TypesGen.expectBoolean(executeGeneric(frame));
  }

  public ParserClassRef executeParserClassRef(final VirtualFrame frame) throws UnexpectedResultException {
	return TypesGen.expectParserClassRef(executeGeneric(frame));
  }

  public FunctionRef executeFunctionRef(final VirtualFrame frame) throws UnexpectedResultException {
	return TypesGen.expectFunctionRef(executeGeneric(frame));
  }

  public Object[] executeObjectArray(final VirtualFrame frame) throws UnexpectedResultException {
	return TypesGen.expectObjectArray(executeGeneric(frame));
  }
}

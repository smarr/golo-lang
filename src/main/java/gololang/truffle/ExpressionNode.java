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

  public long executeLong(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectLong(executeGeneric(frame));
  }

  public double executeDouble(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectDouble(executeGeneric(frame));
  }

  public float executeFloat(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectFloat(executeGeneric(frame));
  }

  public char executeCharacter(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectCharacter(executeGeneric(frame));
  }

  public String executeString(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectString(executeGeneric(frame));
  }

  public ParserClassRef executeParserClassRef(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectParserClassRef(executeGeneric(frame));
  }

  public FunctionRef executeFunctionRef(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectFunctionRef(executeGeneric(frame));
  }

  public Throwable executeThrowable(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectThrowable(executeGeneric(frame));
  }

  public Object[] executeObjectArray(final VirtualFrame frame) throws UnexpectedResultException {
    return TypesGen.expectObjectArray(executeGeneric(frame));
  }
}

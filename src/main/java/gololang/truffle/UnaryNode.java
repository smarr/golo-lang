package gololang.truffle;

import com.oracle.truffle.api.dsl.NodeChild;


@NodeChild(value = "expr", type = ExpressionNode.class)
public abstract class UnaryNode extends ExpressionNode { }

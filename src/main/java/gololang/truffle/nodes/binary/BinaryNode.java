package gololang.truffle.nodes.binary;

import gololang.truffle.ExpressionNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;


@NodeChildren({
  @NodeChild(value = "left",  type = ExpressionNode.class),
  @NodeChild(value = "right", type = ExpressionNode.class)
})
public abstract class BinaryNode extends ExpressionNode { }

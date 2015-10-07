package gololang.truffle.nodes.controlflow;

import fr.insalyon.citi.golo.compiler.ir.GoloFunction;
import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.ir.ModuleImport;
import fr.insalyon.citi.golo.runtime.FunctionCallSupport;
import gololang.Predefined;
import gololang.truffle.EvalArgumentsNode;
import gololang.truffle.ExpressionNode;
import gololang.truffle.Function;
import gololang.truffle.NotYetImplemented;
import gololang.truffle.PreEvaluated;
import gololang.truffle.nodes.unary.PrintlnNodeGen;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;

public abstract class FunctionInvocationNode extends ExpressionNode implements PreEvaluated {

  // TODO:?? name.replaceAll("#", "\\.")
  protected final String name;
  protected final GoloModule module;
  @Child protected EvalArgumentsNode argumentsNode;


  protected FunctionInvocationNode(final String name, final GoloModule module,
      final EvalArgumentsNode argumentsNode) {
    this.name      = name;
    this.module    = module;
    this.argumentsNode = argumentsNode;
  }

  public static FunctionInvocationNode create(final String name, final GoloModule module,
      final EvalArgumentsNode argumentsNode) {
    return new UninitializedFunctionInvocationNode(name, module, argumentsNode);
  }

  public abstract Object executeEvaluated(VirtualFrame frame, Object[] args);

  @Override
  public final Object doEvaluated(final VirtualFrame frame, final Object[] args) {
    return executeEvaluated(frame, args);
  }

  @Override
  public Object executeGeneric(final VirtualFrame frame) {
    Object[] args = argumentsNode.executeObjectArray(frame);
    return executeEvaluated(frame, args);
  }

  protected static final class DirectFunctionInvokeNode extends FunctionInvocationNode {
    @Child protected DirectCallNode call;

    protected DirectFunctionInvokeNode(final FunctionInvocationNode uninit, final Function function) {
      super(uninit.name, uninit.module, uninit.argumentsNode);
      call = Truffle.getRuntime().createDirectCallNode(function.getCallTarget());
    }

    @Override
    public Object executeEvaluated(final VirtualFrame frame, final Object[] args) {
      return call.call(frame, args);
    }
  }

  protected static final class UninitializedFunctionInvocationNode extends FunctionInvocationNode {

    protected UninitializedFunctionInvocationNode(final String name,
        final GoloModule module, final EvalArgumentsNode argumentsNode) {
      super(name, module, argumentsNode);
    }

    @Override
    public Object executeEvaluated(final VirtualFrame frame, final Object[] args) {
      return specialize(args).
          doEvaluated(frame, args);
    }

    private PreEvaluated specialize(final Object[] args) {
      Object lookupResult = lookup(args);

      if (lookupResult instanceof Function) {
        return replace(new DirectFunctionInvokeNode(this, (Function) lookupResult));
      } else if (lookupResult instanceof PreEvaluated) {
        return (PreEvaluated) replace((ExpressionNode) lookupResult);
      }
      throw new NotYetImplemented();
    }

    private Object lookup(final Object[] args) {
      try {
        return lookup(args, null, new String[]{});
      } catch (NoSuchMethodError | IllegalAccessException e) {
        e.printStackTrace();
        throw new NotYetImplemented();
      }
    }

    private Object findClassWithStaticMethodOrField(final Object[] args) {
      int methodClassSeparatorIndex = name.lastIndexOf(".");
      if (methodClassSeparatorIndex >= 0) {
        String className = name.substring(0, methodClassSeparatorIndex);
        String methodName = name.substring(methodClassSeparatorIndex + 1);
        try {
          Class<?> targetClass = Class.forName(className, true, getClass().getClassLoader());
          return FunctionCallSupport.findStaticMethodOrField(targetClass, methodName, args);
        } catch (ClassNotFoundException ignored) {
        }
      }
      return null;
    }

    private Object findStaticMethodOrField(final Object[] arguments) {
      for (GoloFunction fun : module.getFunctions()) {
        if (functionMatches(name, arguments, fun)) {
          return fun;
        }
      }

      // TODO: more cases not covered
      return null;
    }

    public static boolean functionMatches(final String name, final Object[] arguments, final GoloFunction fun) {
      if (fun.getName().equals(name)) {  // TODO: do I need this check? `&& isStatic(method.getModifiers())`
        if (fun.isDecorated()) {
         return true;
        } else {
          if (arguments.length == fun.getArity()) {
            return true;
          }
        }
      }
      return false;
    }


    private Object findClassWithStaticMethodOrFieldFromImports(final Object[] args) {
      String[] imports = getImportStrings();

      String[] classAndMethod = null;
      final int classAndMethodSeparator = name.lastIndexOf(".");
      if (classAndMethodSeparator > 0) {
        classAndMethod = new String[]{
            name.substring(0, classAndMethodSeparator),
            name.substring(classAndMethodSeparator + 1)
        };
      }
      for (String importClassName : imports) {
        try {
          Class<?> importClass;
          try {
            importClass = Class.forName(importClassName, true, getClass().getClassLoader());
          } catch (ClassNotFoundException expected) {
            if (classAndMethod == null) {
              throw expected;
            }
            importClass = Class.forName(importClassName + "." + classAndMethod[0], true, getClass().getClassLoader());
          }
          String lookup = (classAndMethod == null) ? name : classAndMethod[1];

          if (importClass == Predefined.class) {
            switch (lookup) {
              case "println":
                return PrintlnNodeGen.create(argumentsNode.getArgumentNodes()[0]);
            }
          }

          Object result = FunctionCallSupport.findStaticMethodOrField(importClass, lookup, args);
          if (result != null) {
            return result;
          }
        } catch (ClassNotFoundException ignored) {
        }
      }
      return null;
    }

    private String[] getImportStrings() {
      Set<ModuleImport> moduleImports = module.getImports();
      String[] imports = new String[moduleImports.size()];

      int i = 0;
      for (ModuleImport imp : moduleImports) {
        imports[i] = imp.getPackageAndClass().toString();
        i++;
      }
      return imports;
    }

    protected Object lookup(final Object[] args,
        final MethodType type,
        final String[] argumentNames) throws NoSuchMethodError,
        IllegalAccessException {
      // TODO: needs to be done on Golo level, is not need from java code.
      Object result = findStaticMethodOrField(args);
      if (result == null) {
        result = findClassWithStaticMethodOrField(args);
      }
      if (result == null) {
        result = findClassWithStaticMethodOrFieldFromImports(args);
      }
      if (result == null) {
        // TODO result = findClassWithConstructor(args);
      }
      if (result == null) {
    	// TODO result = findClassWithConstructorFromImports(args);
      }
      if (result == null) {
        throw new NoSuchMethodError(name /* + type.toMethodDescriptorString() */);
      }

      if (result instanceof Method) {
    	throw new NotYetImplemented();
      } else if (result instanceof Constructor) {
        throw new NotYetImplemented();
      } else if (result instanceof GoloFunction) {
        return ((GoloFunction) result).getTruffleFunction();
      } else if (result instanceof PreEvaluated) {
        return result;
      } else {
    	throw new NotYetImplemented();
      }
    }
  }
}

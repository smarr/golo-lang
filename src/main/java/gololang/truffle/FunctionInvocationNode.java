package gololang.truffle;

import static fr.insalyon.citi.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static fr.insalyon.citi.golo.runtime.DecoratorsHelper.isMethodDecorated;
import static fr.insalyon.citi.golo.runtime.FunctionCallSupport.checkLocalFunctionCallFromSameModuleAugmentation;
import static fr.insalyon.citi.golo.runtime.TypeMatching.canAssign;
import static fr.insalyon.citi.golo.runtime.TypeMatching.haveEnoughArgumentsForVarargs;
import static fr.insalyon.citi.golo.runtime.TypeMatching.haveSameNumberOfArguments;
import static fr.insalyon.citi.golo.runtime.TypeMatching.isLastArgumentAnArray;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;

import fr.insalyon.citi.golo.compiler.ir.GoloFunction;
import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.ir.ModuleImport;
import fr.insalyon.citi.golo.runtime.FunctionCallSupport;

public abstract class FunctionInvocationNode extends ExpressionNode {

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
  public Object executeGeneric(final VirtualFrame frame) {
    Object[] args = argumentsNode.executeObjectArray(frame);
    return executeEvaluated(frame, args);
  }

  protected static final class MethodHandleInvokeNode extends FunctionInvocationNode {
    private final MethodHandle method;
    public MethodHandleInvokeNode(final FunctionInvocationNode uninit, final MethodHandle method) {
      super(uninit.name, uninit.module, uninit.argumentsNode);
      this.method = method;
    }

    @Override
    public Object executeEvaluated(final VirtualFrame frame, final Object[] args) {
      try {
        return method.invokeWithArguments(args);
      } catch (Throwable e) {
        throw new NotYetImplemented(); // TODO: think, we will need to wrap this one
      }
    }
  }

  protected static final class DirectFunctionInvokeNode extends FunctionInvocationNode {
    @Child protected DirectCallNode call;

    protected DirectFunctionInvokeNode(final FunctionInvocationNode uninit, final Function function) {
      super(uninit.name, uninit.module, uninit.argumentsNode);
      CallTarget ct = function.getCallTarget();
      if (ct == null) {
        ct = Truffle.getRuntime().createCallTarget(function);
      }
      call = Truffle.getRuntime().createDirectCallNode(ct);
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
          executeEvaluated(frame, args);
    }

    private FunctionInvocationNode specialize(final Object[] args) {
      Object lookupResult = lookup(args);

      if (lookupResult instanceof MethodHandle) {
        return replace(new MethodHandleInvokeNode(this, (MethodHandle) lookupResult));
      } else if (lookupResult instanceof Function) {
        return replace(new DirectFunctionInvokeNode(this, (Function) lookupResult));
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

  // TODO: more cases not covered???
  //    for (Method method : klass.getDeclaredMethods()) {
  //      if (methodMatches(name, arguments, method)) {
  //        return method;
  //      }
  //    }
  //    for (Method method : klass.getMethods()) {
  //      if (methodMatches(name, arguments, method)) {
  //        return method;
  //      }
  //    }
  //    if (arguments.length == 0) {
  //      for (Field field : klass.getDeclaredFields()) {
  //        if (fieldMatches(name, field)) {
  //          return field;
  //        }
  //      }
  //      for (Field field : klass.getFields()) {
  //        if (fieldMatches(name, field)) {
  //          return field;
  //        }
  //      }
  //    }
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
  // TODO figure out whether the tests above are sufficient
  //        Class<?>[] parameterTypes = method.getParameterTypes();
  //        if (arguments.length == fun.getArity() || haveEnoughArgumentsForVarargs(arguments, method, parameterTypes)) {
  //          if (canAssign(parameterTypes, arguments, method.isVarArgs())) {
  //            return true;
  //          }
  //        }
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

    private Object findClassWithConstructor(final Object[] args) {
      try {
        Class<?> targetClass = Class.forName(name, true, getClass().getClassLoader());
        for (Constructor<?> constructor : targetClass.getConstructors()) {
          Class<?>[] parameterTypes = constructor.getParameterTypes();
          if (haveSameNumberOfArguments(args, parameterTypes) || haveEnoughArgumentsForVarargs(args, constructor, parameterTypes)) {
            if (canAssign(parameterTypes, args, constructor.isVarArgs())) {
              return constructor;
            }
          }
        }
      } catch (ClassNotFoundException ignored) {
      }
      return null;
    }

    private Object findClassWithConstructorFromImports(final Object[] args) {
  //    String[] imports = getImportStrings();
  //    for (String imported : imports) {
  //      Object result = findClassWithConstructor(imported + "." + classname, args);
  //      if (result != null) {
  //        return result;
  //      }
  //      if (imported.endsWith(classname)) {
  //        result = findClassWithConstructor(imported, args);
  //        if (result != null) {
  //          return result;
  //        }
  //      }
  //    }
      return null;
    }

    protected Object lookup(final Object[] args,
        final MethodType type,
        final String[] argumentNames) throws NoSuchMethodError,
        IllegalAccessException {
      MethodHandle handle = null;
      // TODO: needs to be done on Golo level, is not need from java code.
      Object result = findStaticMethodOrField(args);
      if (result == null) {
        result = findClassWithStaticMethodOrField(args);
      }
      if (result == null) {
        result = findClassWithStaticMethodOrFieldFromImports(args);
      }
      if (result == null) {
        result = findClassWithConstructor(args);
      }
      if (result == null) {
        result = findClassWithConstructorFromImports(args);
      }
      if (result == null) {
        throw new NoSuchMethodError(name /* + type.toMethodDescriptorString() */);
      }

      Class<?>[] types = null;
      Lookup caller = MethodHandles.lookup();
      if (result instanceof Method) {
        Method method = (Method) result;
        checkLocalFunctionCallFromSameModuleAugmentation(method, null);
        if (isMethodDecorated(method)) {
          handle = getDecoratedMethodHandle(caller, method, type.parameterCount());
        } else {
          types = method.getParameterTypes();
          //TODO improve varargs support on named arguments. Matching the last param type + according argument
          if (method.isVarArgs() && (isLastArgumentAnArray(types.length, args) || argumentNames.length > 0)) {
            handle = caller.unreflect(method).asFixedArity().asType(type);
          } else {
            handle = caller.unreflect(method); //TODO:?? .asType(type);
          }
        }
        if (argumentNames.length > 0) {
          NotYetImplemented.t();
  //        handle = reorderArguments(method, handle, argumentNames);
        }
      } else if (result instanceof Constructor) {
        Constructor<?> constructor = (Constructor<?>) result;
        types = constructor.getParameterTypes();
        if (constructor.isVarArgs() && isLastArgumentAnArray(types.length, args)) {
          handle = caller.unreflectConstructor(constructor).asFixedArity().asType(type);
        } else {
          handle = caller.unreflectConstructor(constructor).asType(type);
        }
      } else if (result instanceof GoloFunction) {
        return ((GoloFunction) result).getTruffleFunction();
      } else {
        Field field = (Field) result;
        handle = caller.unreflectGetter(field).asType(type);
      }
      // TODO:
  //    handle = insertSAMFilter(handle, callSite.callerLookup, types, 0);
      return handle;
    }
  }
}

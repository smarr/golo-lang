/*
 * Copyright 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.insalyon.citi.golo.runtime;

import static fr.insalyon.citi.golo.runtime.DecoratorsHelper.getDecoratedMethodHandle;
import static fr.insalyon.citi.golo.runtime.DecoratorsHelper.isMethodDecorated;
import static fr.insalyon.citi.golo.runtime.TypeMatching.canAssign;
import static fr.insalyon.citi.golo.runtime.TypeMatching.haveEnoughArgumentsForVarargs;
import static fr.insalyon.citi.golo.runtime.TypeMatching.haveSameNumberOfArguments;
import static fr.insalyon.citi.golo.runtime.TypeMatching.isFunctionalInterface;
import static fr.insalyon.citi.golo.runtime.TypeMatching.isLastArgumentAnArray;
import static fr.insalyon.citi.golo.runtime.TypeMatching.isSAM;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodType.methodType;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import gololang.FunctionReference;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;


public final class FunctionCallSupport {

  static class FunctionCallSite extends MutableCallSite {

    final Lookup callerLookup;
    final String name;
    final boolean constant;
    final String[] argumentNames;

    FunctionCallSite(final MethodHandles.Lookup callerLookup, final String name, final MethodType type, final boolean constant, final String... argumentNames) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
      this.constant = constant;
      this.argumentNames = argumentNames;
    }
  }

  private static final MethodHandle FALLBACK;
  private static final MethodHandle SAM_FILTER;
  private static final MethodHandle FUNCTIONAL_INTERFACE_FILTER;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      FALLBACK = lookup.findStatic(
          FunctionCallSupport.class,
          "fallback",
          methodType(Object.class, FunctionCallSite.class, Object[].class));
      SAM_FILTER = lookup.findStatic(
          FunctionCallSupport.class,
          "samFilter",
          methodType(Object.class, Class.class, Object.class));
      FUNCTIONAL_INTERFACE_FILTER = lookup.findStatic(
          FunctionCallSupport.class,
          "functionalInterfaceFilter",
          methodType(Object.class, Lookup.class, Class.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static Object samFilter(final Class<?> type, final Object value) {
    if (value instanceof FunctionReference) {
      return MethodHandleProxies.asInterfaceInstance(type, ((FunctionReference) value).handle());
    }
    return value;
  }

  public static Object functionalInterfaceFilter(final Lookup caller, final Class<?> type, final Object value) throws Throwable {
    if (value instanceof FunctionReference) {
      return asFunctionalInterface(caller, type, ((FunctionReference) value).handle());
    }
    return value;
  }

  public static Object asFunctionalInterface(final Lookup caller, final Class<?> type, final MethodHandle handle) throws Throwable {
    for (Method method : type.getMethods()) {
      if (!method.isDefault() && !isStatic(method.getModifiers())) {
        MethodType lambdaType = methodType(method.getReturnType(), method.getParameterTypes());
        CallSite callSite = LambdaMetafactory.metafactory(
            caller,
            method.getName(),
            methodType(type),
            lambdaType,
            handle,
            lambdaType);
        return callSite.dynamicInvoker().invoke();
      }
    }
    throw new RuntimeException("Could not convert " + handle + " to a functional interface of type " + type);
  }

  public static CallSite bootstrap(final Lookup caller, final String name, final MethodType type, final Object... bsmArgs) throws IllegalAccessException, ClassNotFoundException {
    boolean constant = ((int) bsmArgs[0]) == 1;
    String[] argumentNames = new String[bsmArgs.length - 1];
    for (int i = 0; i < bsmArgs.length - 1; i++) {
      argumentNames[i] = (String) bsmArgs[i + 1];
    }
    FunctionCallSite callSite = new FunctionCallSite(caller, name.replaceAll("#", "\\."), type, constant, argumentNames);
    MethodHandle fallbackHandle = FALLBACK.
        bindTo(callSite).
        asCollector(Object[].class, type.parameterCount()).
        asType(type);
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  public static Object fallback(final FunctionCallSite callSite, final Object[] args) throws Throwable {
    String functionName = callSite.name;
    MethodType type = callSite.type();
    Lookup caller = callSite.callerLookup;
    Class<?> callerClass = caller.lookupClass();
    String[] argumentNames = callSite.argumentNames;

    MethodHandle handle = lookup(callSite, args, functionName, type, caller,
        callerClass, argumentNames);

    if (callSite.constant) {
      Object constantValue = handle.invokeWithArguments(args);
      MethodHandle constant;
      if (constantValue == null) {
        constant = MethodHandles.constant(Object.class, constantValue);
      } else {
        constant = MethodHandles.constant(constantValue.getClass(), constantValue);
      }
      constant = MethodHandles.dropArguments(constant, 0, type.parameterArray());
      callSite.setTarget(constant.asType(type));
      return constantValue;
    } else {
      callSite.setTarget(handle);
      return handle.invokeWithArguments(args);
    }
  }

  public static MethodHandle lookup(final FunctionCallSite callSite, final Object[] args,
      final String functionName, final MethodType type, final Lookup caller,
      final Class<?> callerClass, final String[] argumentNames) throws NoSuchMethodError,
      IllegalAccessException {
    MethodHandle handle = null;
    Object result = null; // TODO: needs to be done on Golo level, is not need from java code. findStaticMethodOrField(callerClass, functionName, args);
    if (result == null) {
      result = findClassWithStaticMethodOrField(callerClass, functionName, args);
    }
    if (result == null) {
      result = findClassWithStaticMethodOrFieldFromImports(callerClass, functionName, args);
    }
    if (result == null) {
      result = findClassWithConstructor(callerClass, functionName, args);
    }
    if (result == null) {
      result = findClassWithConstructorFromImports(callerClass, functionName, args);
    }
    if (result == null) {
      throw new NoSuchMethodError(functionName + type.toMethodDescriptorString());
    }

    Class<?>[] types = null;
    if (result instanceof Method) {
      Method method = (Method) result;
      checkLocalFunctionCallFromSameModuleAugmentation(method, callerClass.getName());
      if (isMethodDecorated(method)) {
        handle = getDecoratedMethodHandle(caller, method, type.parameterCount());
      } else {
        types = method.getParameterTypes();
        //TODO improve varargs support on named arguments. Matching the last param type + according argument
        if (method.isVarArgs() && (isLastArgumentAnArray(types.length, args) || argumentNames.length > 0)) {
          handle = caller.unreflect(method).asFixedArity().asType(type);
        } else {
          handle = caller.unreflect(method).asType(type);
        }
      }
      if (argumentNames.length > 0) {
        handle = reorderArguments(method, handle, argumentNames);
      }
    } else if (result instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) result;
      types = constructor.getParameterTypes();
      if (constructor.isVarArgs() && isLastArgumentAnArray(types.length, args)) {
        handle = caller.unreflectConstructor(constructor).asFixedArity().asType(type);
      } else {
        handle = caller.unreflectConstructor(constructor).asType(type);
      }
    } else {
      Field field = (Field) result;
      handle = caller.unreflectGetter(field).asType(type);
    }
    handle = insertSAMFilter(handle, callSite.callerLookup, types, 0);
    return handle;
  }

  public static MethodHandle reorderArguments(final Method method, final MethodHandle handle, final String[] argumentNames) {
    if (Arrays.stream(method.getParameters()).allMatch(p -> p.isNamePresent())) {
    String[] parameterNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
        int[] argumentsOrder = new int[parameterNames.length];
        for (int i = 0; i < argumentNames.length; i++) {
          int actualPosition = -1;
          for (int j = 0; j < parameterNames.length; j++) {
            if (parameterNames[j].equals(argumentNames[i])) {
              actualPosition = j;
            }
          }
          if (actualPosition == -1) {
            throw new IllegalArgumentException("Argument name " + argumentNames[i] + " not in parameter names used in declaration: " + method.getName() + Arrays.toString(parameterNames));
          }
          argumentsOrder[actualPosition] = i;
        }
        return permuteArguments(handle, handle.type(), argumentsOrder);
      }
    return handle;
  }

  public static MethodHandle insertSAMFilter(MethodHandle handle, final Lookup caller, final Class<?>[] types, final int startIndex) {
    if (types != null) {
      for (int i = 0; i < types.length; i++) {
        if (isSAM(types[i])) {
          handle = MethodHandles.filterArguments(handle, startIndex + i, SAM_FILTER.bindTo(types[i]));
        } else if (isFunctionalInterface(types[i])) {
          handle = MethodHandles.filterArguments(handle, startIndex + i, FUNCTIONAL_INTERFACE_FILTER.bindTo(caller).bindTo(types[i]));
        }
      }
    }
    return handle;
  }

  public static void checkLocalFunctionCallFromSameModuleAugmentation(final Method method, final String callerClassName) {
    if (isPrivate(method.getModifiers()) && callerClassName.contains("$")) {
      String prefix = callerClassName.substring(0, callerClassName.indexOf("$"));
      if (method.getDeclaringClass().getName().equals(prefix)) {
        method.setAccessible(true);
      }
    }
  }

  private static Object findClassWithConstructorFromImports(final Class<?> callerClass, final String classname, final Object[] args) {
    String[] imports = Module.imports(callerClass);
    for (String imported : imports) {
      Object result = findClassWithConstructor(callerClass, imported + "." + classname, args);
      if (result != null) {
        return result;
      }
      if (imported.endsWith(classname)) {
        result = findClassWithConstructor(callerClass, imported, args);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  private static Object findClassWithConstructor(final Class<?> callerClass, final String classname, final Object[] args) {
    try {
      Class<?> targetClass = Class.forName(classname, true, callerClass.getClassLoader());
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

  private static Object findClassWithStaticMethodOrFieldFromImports(final Class<?> callerClass, final String functionName, final Object[] args) {
    String[] imports = Module.imports(callerClass);
    String[] classAndMethod = null;
    final int classAndMethodSeparator = functionName.lastIndexOf(".");
    if (classAndMethodSeparator > 0) {
      classAndMethod = new String[]{
          functionName.substring(0, classAndMethodSeparator),
          functionName.substring(classAndMethodSeparator + 1)
      };
    }
    for (String importClassName : imports) {
      try {
        Class<?> importClass;
        try {
          importClass = Class.forName(importClassName, true, callerClass.getClassLoader());
        } catch (ClassNotFoundException expected) {
          if (classAndMethod == null) {
            throw expected;
          }
          importClass = Class.forName(importClassName + "." + classAndMethod[0], true, callerClass.getClassLoader());
        }
        String lookup = (classAndMethod == null) ? functionName : classAndMethod[1];
        Object result = findStaticMethodOrField(importClass, lookup, args);
        if (result != null) {
          return result;
        }
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }

  private static Object findClassWithStaticMethodOrField(final Class<?> callerClass, final String functionName, final Object[] args) {
    int methodClassSeparatorIndex = functionName.lastIndexOf(".");
    if (methodClassSeparatorIndex >= 0) {
      String className = functionName.substring(0, methodClassSeparatorIndex);
      String methodName = functionName.substring(methodClassSeparatorIndex + 1);
      try {
        Class<?> targetClass = Class.forName(className, true, callerClass.getClassLoader());
        return findStaticMethodOrField(targetClass, methodName, args);
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }

  public static Object findStaticMethodOrField(final Class<?> klass, final String name, final Object[] arguments) {
    for (Method method : klass.getDeclaredMethods()) {
      if (methodMatches(name, arguments, method)) {
        return method;
      }
    }
    for (Method method : klass.getMethods()) {
      if (methodMatches(name, arguments, method)) {
        return method;
      }
    }
    if (arguments.length == 0) {
      for (Field field : klass.getDeclaredFields()) {
        if (fieldMatches(name, field)) {
          return field;
        }
      }
      for (Field field : klass.getFields()) {
        if (fieldMatches(name, field)) {
          return field;
        }
      }
    }
    return null;
  }

  public static boolean methodMatches(final String name, final Object[] arguments, final Method method) {
    if (method.getName().equals(name) && isStatic(method.getModifiers())) {
      if (isMethodDecorated(method)) {
       return true;
      } else {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (haveSameNumberOfArguments(arguments, parameterTypes) || haveEnoughArgumentsForVarargs(arguments, method, parameterTypes)) {
          if (canAssign(parameterTypes, arguments, method.isVarArgs())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean fieldMatches(final String name, final Field field) {
    return field.getName().equals(name) && isStatic(field.getModifiers());
  }
}

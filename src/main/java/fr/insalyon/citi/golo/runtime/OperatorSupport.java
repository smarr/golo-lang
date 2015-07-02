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

import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.HashSet;
import java.util.Set;

public class OperatorSupport {

  static class MonomorphicInlineCache extends MutableCallSite {

    final MethodHandles.Lookup callerLookup;
    final String name;
    MethodHandle fallback;

    MonomorphicInlineCache(final MethodHandles.Lookup callerLookup, final String name, final MethodType type) {
      super(type);
      this.callerLookup = callerLookup;
      this.name = name;
    }
  }

  private static final MethodHandle GUARD_1;
  private static final MethodHandle FALLBACK_1;

  private static final MethodHandle GUARD_2;
  private static final MethodHandle FALLBACK_2;

  private static final Set<String> NO_GUARD_OPERATORS = new HashSet<String>() {
    {
      add("is");
      add("isnt");
      add("oftype");

      add("equals");
      add("notequals");

      add("more");
      add("less");
      add("moreorequals");
      add("lessorequals");

      add("orifnull");
    }
  };

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      GUARD_1 = lookup.findStatic(
          OperatorSupport.class,
          "guard_1",
          methodType(boolean.class, Class.class, Object.class));

      FALLBACK_1 = lookup.findStatic(
          OperatorSupport.class,
          "fallback_1",
          methodType(Object.class, MonomorphicInlineCache.class, Object[].class));

      GUARD_2 = lookup.findStatic(
          OperatorSupport.class,
          "guard_2",
          methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));

      FALLBACK_2 = lookup.findStatic(
          OperatorSupport.class,
          "fallback_2",
          methodType(Object.class, MonomorphicInlineCache.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error("Could not bootstrap the required method handles", e);
    }
  }

  public static boolean guard_1(final Class<?> expected, final Object arg) {
    Class<?> t = (arg == null) ? Object.class : arg.getClass();
    return (t == expected);
  }

  public static boolean guard_2(final Class<?> expected1, final Class<?> expected2, final Object arg1, final Object arg2) {
    Class<?> t1 = (arg1 == null) ? Object.class : arg1.getClass();
    Class<?> t2 = (arg2 == null) ? Object.class : arg2.getClass();
    return (t1 == expected1) && (t2 == expected2);
  }

  public static Object fallback_1(final MonomorphicInlineCache inlineCache, final Object[] args) throws Throwable {

    Class<?> argClass = (args[0] == null) ? Object.class : args[0].getClass();
    MethodHandle target;

    try {
      target = inlineCache.callerLookup.findStatic(
          OperatorSupport.class, inlineCache.name, methodType(Object.class, argClass));
    } catch (Throwable t1) {
      try {
        target = inlineCache.callerLookup.findStatic(
            OperatorSupport.class, inlineCache.name + "_fallback", methodType(Object.class, Object.class));
      } catch (Throwable t2) {
        return reject(args[0], inlineCache.name);
      }
    }
    target = target.asType(methodType(Object.class, Object.class));

    MethodHandle guard = GUARD_1.bindTo(argClass);

    MethodHandle guardedTarget = guardWithTest(guard, target, inlineCache.fallback);
    inlineCache.setTarget(guardedTarget);
    return target.invokeWithArguments(args);
  }

  public static Object fallback_2(final MonomorphicInlineCache inlineCache, final Object[] args) throws Throwable {

    Class<?> arg1Class = (args[0] == null) ? Object.class : args[0].getClass();
    Class<?> arg2Class = (args[1] == null) ? Object.class : args[1].getClass();
    MethodHandle target;

    try {
      target = inlineCache.callerLookup.findStatic(
          OperatorSupport.class, inlineCache.name, methodType(Object.class, arg1Class, arg2Class));
    } catch (Throwable t1) {
      try {
        target = inlineCache.callerLookup.findStatic(
            OperatorSupport.class, inlineCache.name + "_fallback", methodType(Object.class, Object.class, Object.class));
      } catch (Throwable t2) {
        return reject(args[0], args[1], inlineCache.name);
      }
    }
    target = target.asType(methodType(Object.class, Object.class, Object.class));

    MethodHandle guard = insertArguments(GUARD_2, 0, arg1Class, arg2Class);

    MethodHandle guardedTarget = guardWithTest(guard, target, inlineCache.fallback);
    inlineCache.setTarget(guardedTarget);
    return target.invokeWithArguments(args);
  }

  public static CallSite bootstrap(final MethodHandles.Lookup caller, final String name, final MethodType type, final int arity) throws NoSuchMethodException, IllegalAccessException {

    if (NO_GUARD_OPERATORS.contains(name)) {
      MethodHandle target = caller.findStatic(OperatorSupport.class, name + "_noguard",
          methodType(Object.class, Object.class, Object.class));
      return new ConstantCallSite(target);
    }

    MonomorphicInlineCache callSite = new MonomorphicInlineCache(caller, name, type);
    MethodHandle fallback;
    if (arity == 2) {
      fallback = FALLBACK_2;
    } else {
      fallback = FALLBACK_1;
    }
    MethodHandle fallbackHandle = fallback
        .bindTo(callSite)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    callSite.fallback = fallbackHandle;
    callSite.setTarget(fallbackHandle);
    return callSite;
  }

  // arithmetic (generated, use generate_math.rb) ......................................................................

  public static Object plus(final Character a, final Character b) {
    return a + b;
  }

  public static Object minus(final Character a, final Character b) {
    return a - b;
  }

  public static Object divide(final Character a, final Character b) {
    return a / b;
  }

  public static Object times(final Character a, final Character b) {
    return a * b;
  }

  public static Object modulo(final Character a, final Character b) {
    return a % b;
  }

  public static Object plus(final Integer a, final Integer b) {
    return a + b;
  }

  public static Object minus(final Integer a, final Integer b) {
    return a - b;
  }

  public static Object divide(final Integer a, final Integer b) {
    return a / b;
  }

  public static Object times(final Integer a, final Integer b) {
    return a * b;
  }

  public static Object modulo(final Integer a, final Integer b) {
    return a % b;
  }

  public static Object plus(final Long a, final Long b) {
    return a + b;
  }

  public static Object minus(final Long a, final Long b) {
    return a - b;
  }

  public static Object divide(final Long a, final Long b) {
    return a / b;
  }

  public static Object times(final Long a, final Long b) {
    return a * b;
  }

  public static Object modulo(final Long a, final Long b) {
    return a % b;
  }

  public static Object plus(final Double a, final Double b) {
    return a + b;
  }

  public static Object minus(final Double a, final Double b) {
    return a - b;
  }

  public static Object divide(final Double a, final Double b) {
    return a / b;
  }

  public static Object times(final Double a, final Double b) {
    return a * b;
  }

  public static Object modulo(final Double a, final Double b) {
    return a % b;
  }

  public static Object plus(final Float a, final Float b) {
    return a + b;
  }

  public static Object minus(final Float a, final Float b) {
    return a - b;
  }

  public static Object divide(final Float a, final Float b) {
    return a / b;
  }

  public static Object times(final Float a, final Float b) {
    return a * b;
  }

  public static Object modulo(final Float a, final Float b) {
    return a % b;
  }

  public static Object plus(final Character a, final Integer b) {
    return ((int) a) + b;
  }

  public static Object minus(final Character a, final Integer b) {
    return ((int) a) - b;
  }

  public static Object divide(final Character a, final Integer b) {
    return ((int) a) / b;
  }

  public static Object times(final Character a, final Integer b) {
    return ((int) a) * b;
  }

  public static Object modulo(final Character a, final Integer b) {
    return ((int) a) % b;
  }

  public static Object plus(final Character a, final Long b) {
    return ((long) a) + b;
  }

  public static Object minus(final Character a, final Long b) {
    return ((long) a) - b;
  }

  public static Object divide(final Character a, final Long b) {
    return ((long) a) / b;
  }

  public static Object times(final Character a, final Long b) {
    return ((long) a) * b;
  }

  public static Object modulo(final Character a, final Long b) {
    return ((long) a) % b;
  }

  public static Object plus(final Character a, final Double b) {
    return ((double) a) + b;
  }

  public static Object minus(final Character a, final Double b) {
    return ((double) a) - b;
  }

  public static Object divide(final Character a, final Double b) {
    return ((double) a) / b;
  }

  public static Object times(final Character a, final Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(final Character a, final Double b) {
    return ((double) a) % b;
  }

  public static Object plus(final Character a, final Float b) {
    return ((float) a) + b;
  }

  public static Object minus(final Character a, final Float b) {
    return ((float) a) - b;
  }

  public static Object divide(final Character a, final Float b) {
    return ((float) a) / b;
  }

  public static Object times(final Character a, final Float b) {
    return ((float) a) * b;
  }

  public static Object modulo(final Character a, final Float b) {
    return ((float) a) % b;
  }

  public static Object plus(final Integer a, final Long b) {
    return ((long) a) + b;
  }

  public static Object minus(final Integer a, final Long b) {
    return ((long) a) - b;
  }

  public static Object divide(final Integer a, final Long b) {
    return ((long) a) / b;
  }

  public static Object times(final Integer a, final Long b) {
    return ((long) a) * b;
  }

  public static Object modulo(final Integer a, final Long b) {
    return ((long) a) % b;
  }

  public static Object plus(final Integer a, final Double b) {
    return ((double) a) + b;
  }

  public static Object minus(final Integer a, final Double b) {
    return ((double) a) - b;
  }

  public static Object divide(final Integer a, final Double b) {
    return ((double) a) / b;
  }

  public static Object times(final Integer a, final Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(final Integer a, final Double b) {
    return ((double) a) % b;
  }

  public static Object plus(final Integer a, final Float b) {
    return ((float) a) + b;
  }

  public static Object minus(final Integer a, final Float b) {
    return ((float) a) - b;
  }

  public static Object divide(final Integer a, final Float b) {
    return ((float) a) / b;
  }

  public static Object times(final Integer a, final Float b) {
    return ((float) a) * b;
  }

  public static Object modulo(final Integer a, final Float b) {
    return ((float) a) % b;
  }

  public static Object plus(final Long a, final Double b) {
    return ((double) a) + b;
  }

  public static Object minus(final Long a, final Double b) {
    return ((double) a) - b;
  }

  public static Object divide(final Long a, final Double b) {
    return ((double) a) / b;
  }

  public static Object times(final Long a, final Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(final Long a, final Double b) {
    return ((double) a) % b;
  }

  public static Object plus(final Long a, final Float b) {
    return ((float) a) + b;
  }

  public static Object minus(final Long a, final Float b) {
    return ((float) a) - b;
  }

  public static Object divide(final Long a, final Float b) {
    return ((float) a) / b;
  }

  public static Object times(final Long a, final Float b) {
    return ((float) a) * b;
  }

  public static Object modulo(final Long a, final Float b) {
    return ((float) a) % b;
  }

  public static Object plus(final Double a, final Float b) {
    return a + ((double) b);
  }

  public static Object minus(final Double a, final Float b) {
    return a - ((double) b);
  }

  public static Object divide(final Double a, final Float b) {
    return a / ((double) b);
  }

  public static Object times(final Double a, final Float b) {
    return a * ((double) b);
  }

  public static Object modulo(final Double a, final Float b) {
    return a % ((double) b);
  }

  public static Object plus(final Integer a, final Character b) {
    return a + ((int) b);
  }

  public static Object minus(final Integer a, final Character b) {
    return a - ((int) b);
  }

  public static Object divide(final Integer a, final Character b) {
    return a / ((int) b);
  }

  public static Object times(final Integer a, final Character b) {
    return a * ((int) b);
  }

  public static Object modulo(final Integer a, final Character b) {
    return a % ((int) b);
  }

  public static Object plus(final Long a, final Character b) {
    return a + ((long) b);
  }

  public static Object minus(final Long a, final Character b) {
    return a - ((long) b);
  }

  public static Object divide(final Long a, final Character b) {
    return a / ((long) b);
  }

  public static Object times(final Long a, final Character b) {
    return a * ((long) b);
  }

  public static Object modulo(final Long a, final Character b) {
    return a % ((long) b);
  }

  public static Object plus(final Double a, final Character b) {
    return a + ((double) b);
  }

  public static Object minus(final Double a, final Character b) {
    return a - ((double) b);
  }

  public static Object divide(final Double a, final Character b) {
    return a / ((double) b);
  }

  public static Object times(final Double a, final Character b) {
    return a * ((double) b);
  }

  public static Object modulo(final Double a, final Character b) {
    return a % ((double) b);
  }

  public static Object plus(final Float a, final Character b) {
    return a + ((float) b);
  }

  public static Object minus(final Float a, final Character b) {
    return a - ((float) b);
  }

  public static Object divide(final Float a, final Character b) {
    return a / ((float) b);
  }

  public static Object times(final Float a, final Character b) {
    return a * ((float) b);
  }

  public static Object modulo(final Float a, final Character b) {
    return a % ((float) b);
  }

  public static Object plus(final Long a, final Integer b) {
    return a + ((long) b);
  }

  public static Object minus(final Long a, final Integer b) {
    return a - ((long) b);
  }

  public static Object divide(final Long a, final Integer b) {
    return a / ((long) b);
  }

  public static Object times(final Long a, final Integer b) {
    return a * ((long) b);
  }

  public static Object modulo(final Long a, final Integer b) {
    return a % ((long) b);
  }

  public static Object plus(final Double a, final Integer b) {
    return a + ((double) b);
  }

  public static Object minus(final Double a, final Integer b) {
    return a - ((double) b);
  }

  public static Object divide(final Double a, final Integer b) {
    return a / ((double) b);
  }

  public static Object times(final Double a, final Integer b) {
    return a * ((double) b);
  }

  public static Object modulo(final Double a, final Integer b) {
    return a % ((double) b);
  }

  public static Object plus(final Float a, final Integer b) {
    return a + ((float) b);
  }

  public static Object minus(final Float a, final Integer b) {
    return a - ((float) b);
  }

  public static Object divide(final Float a, final Integer b) {
    return a / ((float) b);
  }

  public static Object times(final Float a, final Integer b) {
    return a * ((float) b);
  }

  public static Object modulo(final Float a, final Integer b) {
    return a % ((float) b);
  }

  public static Object plus(final Double a, final Long b) {
    return a + ((double) b);
  }

  public static Object minus(final Double a, final Long b) {
    return a - ((double) b);
  }

  public static Object divide(final Double a, final Long b) {
    return a / ((double) b);
  }

  public static Object times(final Double a, final Long b) {
    return a * ((double) b);
  }

  public static Object modulo(final Double a, final Long b) {
    return a % ((double) b);
  }

  public static Object plus(final Float a, final Long b) {
    return a + ((float) b);
  }

  public static Object minus(final Float a, final Long b) {
    return a - ((float) b);
  }

  public static Object divide(final Float a, final Long b) {
    return a / ((float) b);
  }

  public static Object times(final Float a, final Long b) {
    return a * ((float) b);
  }

  public static Object modulo(final Float a, final Long b) {
    return a % ((float) b);
  }

  public static Object plus(final Float a, final Double b) {
    return ((double) a) + b;
  }

  public static Object minus(final Float a, final Double b) {
    return ((double) a) - b;
  }

  public static Object divide(final Float a, final Double b) {
    return ((double) a) / b;
  }

  public static Object times(final Float a, final Double b) {
    return ((double) a) * b;
  }

  public static Object modulo(final Float a, final Double b) {
    return ((double) a) % b;
  }

  // arithmetic fallbacks .............................................................................................

  public static Object plus(final String a, final String b) {
    return a + b;
  }

  public static Object plus_fallback(final Object a, final Object b) {
    if (isNotNullAndString(a) || isNotNullAndString(b)) {
      return String.valueOf(a) + b;
    }
    return reject(a, b, "plus");
  }

  public static Object times_fallback(final Object a, final Object b) {
    if (isInteger(a) && isString(b)) {
      return repeat((String) b, (Integer) a);
    }
    if (isString(a) && isInteger(b)) {
      return repeat((String) a, (Integer) b);
    }
    return reject(a, b, "times");
  }

  private static String repeat(final String string, final int n) {
    StringBuilder builder = new StringBuilder(string);
    for (int i = 1; i < n; i++) {
      builder.append(string);
    }
    return builder.toString();
  }

  // comparisons ......................................................................................................

  public static Object equals_noguard(final Object a, final Object b) {
    return (a == b) || ((a != null) && a.equals(b));
  }

  public static Object notequals_noguard(final Object a, final Object b) {
    return (a != b) && (((a != null) && !a.equals(b)) || ((b != null) && !b.equals(a)));
  }

  @SuppressWarnings("unchecked")
  public static Object less_noguard(final Object a, final Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) < 0;
    }
    return reject(a, b, "less");
  }

  @SuppressWarnings("unchecked")
  public static Object lessorequals_noguard(final Object a, final Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) <= 0;
    }
    return reject(a, b, "lessorequals");
  }

  @SuppressWarnings("unchecked")
  public static Object more_noguard(final Object a, final Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) > 0;
    }
    return reject(a, b, "more");
  }

  @SuppressWarnings("unchecked")
  public static Object moreorequals_noguard(final Object a, final Object b) {
    if (bothNotNull(a, b) && isComparable(a) && isComparable(b)) {
      return ((Comparable) a).compareTo(b) >= 0;
    }
    return reject(a, b, "moreorequals");
  }

  // logic ............................................................................................................

  public static Object not(final Boolean a) {
    return !a;
  }

  public static Object oftype_noguard(final Object a, final Object b) {
    if (isClass(b)) {
      return ((Class<?>) b).isInstance(a);
    }
    return reject(a, b, "oftype");
  }

  public static Object is_noguard(final Object a, final Object b) {
    return a == b;
  }

  public static Object isnt_noguard(final Object a, final Object b) {
    return a != b;
  }

  public static Object orifnull_noguard(final Object a, final Object b) {
    return (a != null) ? a : b;
  }

  // helpers ..........................................................................................................

  private static boolean isNotNullAndString(final Object obj) {
    return (obj != null) && (obj.getClass() == String.class);
  }

  private static boolean bothNotNull(final Object a, final Object b) {
    return (a != null) && (b != null);
  }

  private static boolean isString(final Object obj) {
    return obj.getClass() == String.class;
  }

  private static boolean isInteger(final Object obj) {
    return obj.getClass() == Integer.class;
  }

  private static boolean isComparable(final Object obj) {
    return obj instanceof Comparable<?>;
  }

  private static boolean isClass(final Object obj) {
    return (obj != null) && (obj.getClass() == Class.class);
  }

  private static Object reject(final Object a, final String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.format("Operator %s is not supported for type %s", symbol, a.getClass()));
  }

  private static Object reject(final Object a, final Object b, final String symbol) throws IllegalArgumentException {
    throw new IllegalArgumentException(String.format("Operator %s is not supported for types %s and %s", symbol, a.getClass(), b.getClass()));
  }
}

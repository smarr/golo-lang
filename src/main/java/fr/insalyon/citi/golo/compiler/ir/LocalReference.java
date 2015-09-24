/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.compiler.ir;

public final class LocalReference {

  public static enum Kind {
    CONSTANT, VARIABLE, MODULE_CONSTANT, MODULE_VARIABLE
  }

  private final Kind kind;
  private final String name;
  private final boolean synthetic;
  private final boolean isArgument;

  private int index = -1;

  public LocalReference(Kind kind, String name) {
    this.kind = kind;
    this.name = name;
    this.synthetic = false;
    this.isArgument = false;
  }

  public LocalReference(boolean isArgument, String name) {
    this.kind = Kind.CONSTANT;
    this.name = name;
    assert isArgument == true;
    this.isArgument = true;
    this.synthetic = false;
  }

  public LocalReference(Kind kind, String name, boolean synthetic) {
    this.kind = kind;
    this.name = name;
    this.synthetic = synthetic;
    this.isArgument = false;
  }

  public Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public boolean isSynthetic() {
    return synthetic;
  }
  
  public boolean isArgument() {
	return isArgument;
  }

  public boolean isModuleState() {
    return kind == Kind.MODULE_CONSTANT || kind == Kind.MODULE_VARIABLE;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  @Override
  public String toString() {
    return "LocalReference{" +
        "kind=" + kind +
        ", name='" + name + '\'' +
        ", index=" + index +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LocalReference that = (LocalReference) o;

    if (kind != that.kind) return false;
    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = kind.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}

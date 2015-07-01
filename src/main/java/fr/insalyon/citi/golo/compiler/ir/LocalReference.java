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

  public LocalReference(final Kind kind, final String name) {
    this.kind = kind;
    this.name = name;
    this.synthetic = false;
    this.isArgument = false;
  }

  public LocalReference(final boolean isArgument, final String name) {
    this.kind = Kind.CONSTANT;
    this.name = name;
    assert isArgument == true;
    this.isArgument = true;
    this.synthetic = false;
  }

  public LocalReference(final Kind kind, final String name, final boolean synthetic) {
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

  public boolean isModuleState() {
    return kind == Kind.MODULE_CONSTANT || kind == Kind.MODULE_VARIABLE;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(final int index) {
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
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LocalReference that = (LocalReference) o;

    if (kind != that.kind) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = kind.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  public boolean isArgument() {
    return isArgument;
  }
}

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

package fr.insalyon.citi.golo.doc;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.insalyon.citi.golo.compiler.parser.ASTAdditiveExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTAndExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTAnonymousFunctionInvocation;
import fr.insalyon.citi.golo.compiler.parser.ASTArgument;
import fr.insalyon.citi.golo.compiler.parser.ASTAssignment;
import fr.insalyon.citi.golo.compiler.parser.ASTAugmentDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTBitExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTBlock;
import fr.insalyon.citi.golo.compiler.parser.ASTBreak;
import fr.insalyon.citi.golo.compiler.parser.ASTCase;
import fr.insalyon.citi.golo.compiler.parser.ASTCollectionLiteral;
import fr.insalyon.citi.golo.compiler.parser.ASTCompilationUnit;
import fr.insalyon.citi.golo.compiler.parser.ASTConditionalBranching;
import fr.insalyon.citi.golo.compiler.parser.ASTContinue;
import fr.insalyon.citi.golo.compiler.parser.ASTDecoratorDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTEqualityExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTExpressionStatement;
import fr.insalyon.citi.golo.compiler.parser.ASTForEachLoop;
import fr.insalyon.citi.golo.compiler.parser.ASTForLoop;
import fr.insalyon.citi.golo.compiler.parser.ASTFunction;
import fr.insalyon.citi.golo.compiler.parser.ASTFunctionDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTFunctionInvocation;
import fr.insalyon.citi.golo.compiler.parser.ASTImportDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTInvocationExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTLetOrVar;
import fr.insalyon.citi.golo.compiler.parser.ASTLiteral;
import fr.insalyon.citi.golo.compiler.parser.ASTMatch;
import fr.insalyon.citi.golo.compiler.parser.ASTMethodInvocation;
import fr.insalyon.citi.golo.compiler.parser.ASTModuleDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTMultiplicativeExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTNamedAugmentationDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTOrExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTOrIfNullExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTReference;
import fr.insalyon.citi.golo.compiler.parser.ASTRelationalExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTReturn;
import fr.insalyon.citi.golo.compiler.parser.ASTStructDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTThrow;
import fr.insalyon.citi.golo.compiler.parser.ASTToplevelDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTTryCatchFinally;
import fr.insalyon.citi.golo.compiler.parser.ASTUnaryExpression;
import fr.insalyon.citi.golo.compiler.parser.ASTUnionDeclaration;
import fr.insalyon.citi.golo.compiler.parser.ASTUnionValue;
import fr.insalyon.citi.golo.compiler.parser.ASTWhileLoop;
import fr.insalyon.citi.golo.compiler.parser.ASTerror;
import fr.insalyon.citi.golo.compiler.parser.GoloParserVisitor;
import fr.insalyon.citi.golo.compiler.parser.SimpleNode;
import fr.insalyon.citi.golo.compiler.utils.AbstractRegister;

class FunctionDocumentationsRegister extends AbstractRegister<String, FunctionDocumentation> {
  private static final long serialVersionUID = 1L;

  @Override
  protected Set<FunctionDocumentation> emptyValue() {
    return new TreeSet<>();
  }

  @Override
  protected Map<String, Set<FunctionDocumentation>> initMap() {
    return new TreeMap<>();
  }
}

class ModuleDocumentation implements DocumentationElement {

  private String moduleName;
  private int moduleDefLine;
  private String moduleDocumentation;

  private final Map<String, Integer> imports = new TreeMap<>();
  private final Map<String, Integer> moduleStates = new TreeMap<>();
  private final SortedSet<FunctionDocumentation> functions = new TreeSet<>();
  private final Map<String, AugmentationDocumentation> augmentations = new TreeMap<>();
  private final SortedSet<StructDocumentation> structs = new TreeSet<>();
  private final SortedSet<UnionDocumentation> unions = new TreeSet<>();
  private final Set<NamedAugmentationDocumentation> namedAugmentations = new TreeSet<>();

  ModuleDocumentation(final ASTCompilationUnit compilationUnit) {
    new ModuleVisitor().visit(compilationUnit, null);
  }

  public SortedSet<StructDocumentation> structs() {
    return structs;
  }

  public SortedSet<UnionDocumentation> unions() {
    return unions;
  }

  public SortedSet<FunctionDocumentation> functions() {
    return functions(false);
  }

  public SortedSet<FunctionDocumentation> functions(final boolean withLocal) {
    if (withLocal) {
      return functions;
    }
    TreeSet<FunctionDocumentation> pubFunctions = new TreeSet<>();
    for (FunctionDocumentation f : functions) {
      if (!f.local()) {
        pubFunctions.add(f);
      }
    }
    return pubFunctions;
  }

  public String moduleName() {
    return moduleName;
  }

  @Override
  public String name() {
    return moduleName;
  }

  public int moduleDefLine() {
    return moduleDefLine;
  }

  @Override
  public int line() {
    return moduleDefLine;
  }

  public String moduleDocumentation() {
    return (moduleDocumentation != null) ? moduleDocumentation : "\n";
  }

  @Override
  public String documentation() {
    return moduleDocumentation();
  }

  public Map<String, Integer> moduleStates() {
    return moduleStates;
  }

  public Collection<AugmentationDocumentation> augmentations() {
    return augmentations.values();
  }

  public Collection<NamedAugmentationDocumentation> namedAugmentations() {
    return namedAugmentations;
  }

  public Map<String, Integer> imports() {
    return imports;
  }

  private class ModuleVisitor implements GoloParserVisitor {

    private Deque<Set<FunctionDocumentation>> functionContext = new LinkedList<>();
    private FunctionDocumentation currentFunction = null;
    private UnionDocumentation currentUnion;

    @Override
    public Object visit(final ASTCompilationUnit node, final Object data) {
      functionContext.push(functions);
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(final ASTModuleDeclaration node, final Object data) {
      moduleName = node.getName();
      moduleDefLine = node.getLineInSourceCode();
      moduleDocumentation = node.getDocumentation();
      return data;
    }

    @Override
    public Object visit(final ASTImportDeclaration node, final Object data) {
      imports.put(node.getName(), node.getLineInSourceCode());
      return data;
    }

    @Override
    public Object visit(final ASTToplevelDeclaration node, final Object data) {
      node.childrenAccept(this, data);
      return data;
    }

    @Override
    public Object visit(final ASTStructDeclaration node, final Object data) {
      structs.add(new StructDocumentation()
              .name(node.getName())
              .documentation(node.getDocumentation())
              .line(node.getLineInSourceCode())
              .members(node.getMembers())
      );
      return data;
    }

    @Override
    public Object visit(final ASTUnionDeclaration node, final Object data) {
      this.currentUnion = new UnionDocumentation()
          .name(node.getName())
          .documentation(node.getDocumentation())
          .line(node.getLineInSourceCode());
      unions.add(this.currentUnion);
      return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(final ASTUnionValue node, final Object data) {
      this.currentUnion.addValue(node.getName())
          .documentation(node.getDocumentation())
          .line(node.getLineInSourceCode())
          .members(node.getMembers());
      return data;
    }

    @Override
    public Object visit(final ASTAugmentDeclaration node, final Object data) {
      /* NOTE:
       * if multiple augmentations are defined for the same target
       * only the line and (non empty) documentation of the first one are kept.
       *
       * Maybe we should concatenate documentations since the golodoc merges
       * the functions documentations, but we could then generate not meaningful
       * content...
       */
      String target = node.getName();
      if (!augmentations.containsKey(target)) {
        augmentations.put(target, new AugmentationDocumentation()
                .target(target)
                .augmentationNames(node.getAugmentationNames())
                .line(node.getLineInSourceCode())
        );
      }
      functionContext.push(augmentations.get(target).documentation(node.getDocumentation()));
      node.childrenAccept(this, data);
      functionContext.pop();
      return data;
    }

    @Override
    public Object visit(final ASTNamedAugmentationDeclaration node, final Object data) {
      NamedAugmentationDocumentation augment = new NamedAugmentationDocumentation()
          .name(node.getName())
          .documentation(node.getDocumentation())
          .line(node.getLineInSourceCode());
      namedAugmentations.add(augment);
      functionContext.push(augment);
      node.childrenAccept(this, data);
      functionContext.pop();
      return data;
    }

    @Override
    public Object visit(final ASTFunctionDeclaration node, final Object data) {
      currentFunction = new FunctionDocumentation()
          .name(node.getName())
          .documentation(node.getDocumentation())
          .augmentation(node.isAugmentation())
          .line(node.getLineInSourceCode())
          .local(node.isLocal());
      functionContext.peek().add(currentFunction);
      node.childrenAccept(this, data);
      currentFunction = null;
      return data;
    }

    @Override
    public Object visit(final ASTFunction node, final Object data) {
      if (currentFunction != null) {
        currentFunction
          .arguments(node.getParameters())
          .varargs(node.isVarargs());
      }
      return data;
    }

    @Override
    public Object visit(final ASTLetOrVar node, final Object data) {
      if (node.isModuleState()) {
        moduleStates.put(node.getName(), node.getLineInSourceCode());
      }
      return data;
    }

    @Override
    public Object visit(final ASTContinue node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTBreak node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTThrow node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTWhileLoop node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTForLoop node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTForEachLoop node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTTryCatchFinally node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTUnaryExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTExpressionStatement node, final Object data) { return data; }

    @Override
    public Object visit(final ASTInvocationExpression node, final Object data) { return data; }

    @Override
    public Object visit(final ASTBitExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTMultiplicativeExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTAdditiveExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTRelationalExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTEqualityExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTAndExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTOrExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTOrIfNullExpression node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTMethodInvocation node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTBlock node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTLiteral node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTCollectionLiteral node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTReference node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTAssignment node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTReturn node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTArgument node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTAnonymousFunctionInvocation node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTFunctionInvocation node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTConditionalBranching node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTCase node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTMatch node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTDecoratorDeclaration node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final SimpleNode node, final Object data) {
      return data;
    }

    @Override
    public Object visit(final ASTerror node, final Object data) {
      return data;
    }
  }
}

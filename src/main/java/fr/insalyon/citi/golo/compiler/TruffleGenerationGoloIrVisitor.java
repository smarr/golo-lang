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

package fr.insalyon.citi.golo.compiler;

import static fr.insalyon.citi.golo.compiler.JavaBytecodeUtils.loadInteger;
import static fr.insalyon.citi.golo.runtime.OperatorType.AND;
import static fr.insalyon.citi.golo.runtime.OperatorType.ANON_CALL;
import static fr.insalyon.citi.golo.runtime.OperatorType.ELVIS_METHOD_CALL;
import static fr.insalyon.citi.golo.runtime.OperatorType.METHOD_CALL;
import static fr.insalyon.citi.golo.runtime.OperatorType.OR;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.SWAP;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;

import fr.insalyon.citi.golo.compiler.ir.AbstractInvocation;
import fr.insalyon.citi.golo.compiler.ir.AssignmentStatement;
import fr.insalyon.citi.golo.compiler.ir.BinaryOperation;
import fr.insalyon.citi.golo.compiler.ir.Block;
import fr.insalyon.citi.golo.compiler.ir.ClosureReference;
import fr.insalyon.citi.golo.compiler.ir.CollectionLiteral;
import fr.insalyon.citi.golo.compiler.ir.ConditionalBranching;
import fr.insalyon.citi.golo.compiler.ir.ConstantStatement;
import fr.insalyon.citi.golo.compiler.ir.Decorator;
import fr.insalyon.citi.golo.compiler.ir.ExpressionStatement;
import fr.insalyon.citi.golo.compiler.ir.FunctionInvocation;
import fr.insalyon.citi.golo.compiler.ir.GoloFunction;
import fr.insalyon.citi.golo.compiler.ir.GoloModule;
import fr.insalyon.citi.golo.compiler.ir.GoloStatement;
import fr.insalyon.citi.golo.compiler.ir.LocalReference;
import fr.insalyon.citi.golo.compiler.ir.LoopBreakFlowStatement;
import fr.insalyon.citi.golo.compiler.ir.LoopStatement;
import fr.insalyon.citi.golo.compiler.ir.MethodInvocation;
import fr.insalyon.citi.golo.compiler.ir.ReferenceLookup;
import fr.insalyon.citi.golo.compiler.ir.ReferenceTable;
import fr.insalyon.citi.golo.compiler.ir.ReturnStatement;
import fr.insalyon.citi.golo.compiler.ir.ThrowStatement;
import fr.insalyon.citi.golo.compiler.ir.TryCatchFinally;
import fr.insalyon.citi.golo.compiler.ir.UnaryOperation;
import fr.insalyon.citi.golo.compiler.parser.GoloParser;
import fr.insalyon.citi.golo.runtime.OperatorType;
import gololang.truffle.BinaryNode;
import gololang.truffle.EvalArgumentsNode;
import gololang.truffle.ExpressionNode;
import gololang.truffle.Function;
import gololang.truffle.LocalArgumentReadNode;
import gololang.truffle.LocalVariableReadNode;
import gololang.truffle.LocalVariableWriteNodeGen;
import gololang.truffle.NotYetImplemented;
import gololang.truffle.ThrowNodeGen;
import gololang.truffle.literals.LiteralNode;
import gololang.truffle.literals.LiteralNode.CharacterLiteralNode;
import gololang.truffle.literals.LiteralNode.DoubleLiteralNode;
import gololang.truffle.literals.LiteralNode.FalseLiteralNode;
import gololang.truffle.literals.LiteralNode.FloatLiteralNode;
import gololang.truffle.literals.LiteralNode.IntegerLiteralNode;
import gololang.truffle.literals.LiteralNode.LongLiteralNode;
import gololang.truffle.literals.LiteralNode.NullLiteralNode;
import gololang.truffle.literals.LiteralNode.StringLiteralNode;
import gololang.truffle.literals.LiteralNode.TrueLiteralNode;
import gololang.truffle.nodes.conrolflow.ForLoopNode;
import gololang.truffle.nodes.conrolflow.FunctionInvocationNode;
import gololang.truffle.nodes.conrolflow.IfNode;
import gololang.truffle.nodes.conrolflow.ReturnNode;
import gololang.truffle.nodes.conrolflow.SequenceNode;
import gololang.truffle.nodes.conrolflow.ThrowNode;

public class TruffleGenerationGoloIrVisitor {

  private MethodVisitor methodVisitor;
  private List<?> notSureYetPerhaps_callTargets;
  private Context context;
  private GoloModule module;

  private static class Context {
    private final Deque<ReferenceTable> referenceTableStack = new LinkedList<>();
    private final Deque<FrameDescriptor> frameDescriptors   = new LinkedList<>();

    private final Map<LoopStatement, Label> loopStartMap = new HashMap<>();
    private final Map<LoopStatement, Label> loopEndMap = new HashMap<>();
  }

  public void generateRepresentation(final GoloModule module) {
    this.module  = module;
    this.notSureYetPerhaps_callTargets = new LinkedList<>();
    this.context = new Context();

    module.accept(this);


//    return this.generationResults;
  }

  public void visitModule(final GoloModule module) {
// TODO: we might need to load those imports here
//    for (ModuleImport imp : module.getImports()) {
//      load -> imp.getPackageAndClass().toString();
//    }
    for (GoloFunction function : module.getFunctions()) {
      module.addFunction(function.accept(this));
    }


//    klass = module.getPackageAndClass().toString();
//    jvmKlass = module.getPackageAndClass().toJVMType();
//
//    generateAugmentationsBytecode(module, module.getAugmentations());
//    generateAugmentationsBytecode(module, module.getNamedAugmentations());
//    if (module.getStructs().size() > 0) {
//      JavaBytecodeStructGenerator structGenerator = new JavaBytecodeStructGenerator();
//      for (Struct struct : module.getStructs()) {
//        generationResults.add(structGenerator.compile(struct, sourceFilename));
//      }
//    }
//    if (!module.getUnions().isEmpty()) {
//      JavaBytecodeUnionGenerator unionGenerator = new JavaBytecodeUnionGenerator();
//      for (Union e : module.getUnions()) {
//        generationResults.addAll(unionGenerator.compile(e, sourceFilename));
//      }
//    }
//    for (LocalReference moduleState : module.getModuleState()) {
//      writeModuleState(moduleState);
//    }
//    writeAugmentsMetaData(module.getAugmentations().keySet());
//    writeAugmentationApplicationsMetaData(module.getAugmentationApplications());
  }

  private void writeModuleState(final LocalReference moduleState) {
    NotYetImplemented.t();
//    String name = moduleState.getName();
//    classWriter.visitField(ACC_PRIVATE | ACC_STATIC, name, "Ljava/lang/Object;", null, null).visitEnd();
//
//    MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, name, "()Ljava/lang/Object;", null, null);
//    mv.visitCode();
//    mv.visitFieldInsn(GETSTATIC, jvmKlass, name, "Ljava/lang/Object;");
//    mv.visitInsn(ARETURN);
//    mv.visitMaxs(0, 0);
//    mv.visitEnd();
//
//    mv = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, name, "(Ljava/lang/Object;)V", null, null);
//    mv.visitCode();
//    mv.visitVarInsn(ALOAD, 0);
//    mv.visitFieldInsn(PUTSTATIC, jvmKlass, name, "Ljava/lang/Object;");
//    mv.visitInsn(RETURN);
//    mv.visitMaxs(0, 0);
//    mv.visitEnd();
  }

  private void writeAugmentationApplicationsMetaData(final Map<String, Set<String>> applications) {
    NotYetImplemented.t();
    /* create a metadata method that given a target class name hashcode
     * returns a String array containing the names of applied
     * augmentations
     */

//    int applicationsSize = applications.size();
//    List<String> applicationNames = new ArrayList<>(applications.keySet());
//
//    Label defaultLabel = new Label();
//    Label[] labels = new Label[applicationsSize];
//    int[] keys = new int[applicationsSize];
//    String[][] namesArrays = new String[applicationsSize][];
//    // cases of the switch statement MUST be sorted
//    Collections.sort(applicationNames, new Comparator<String>(){
//      @Override
//      public int compare(final String o1, final String o2) {
//        return Integer.compare(o1.hashCode(), o2.hashCode());
//      }
//    });
//    int i = 0;
//    for (String applicationName : applicationNames) {
//      labels[i] = new Label();
//      keys[i] = applicationName.hashCode();
//      namesArrays[i] = applications.get(applicationName).toArray(new String[applications.get(applicationName).size()]);
//      i++;
//    }
//    methodVisitor = classWriter.visitMethod(
//        ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
//        "$augmentationApplications",
//        "(I)[Ljava/lang/String;",
//        null, null);
//    methodVisitor.visitCode();
//    methodVisitor.visitVarInsn(ILOAD, 0);
//    methodVisitor.visitLookupSwitchInsn(defaultLabel, keys, labels);
//    for (i = 0; i < applicationsSize; i++) {
//      methodVisitor.visitLabel(labels[i]);
//      loadInteger(methodVisitor, namesArrays[i].length);
//      methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
//      for (int j = 0; j < namesArrays[i].length; j++) {
//        methodVisitor.visitInsn(DUP);
//        loadInteger(methodVisitor, j);
//        methodVisitor.visitLdcInsn(namesArrays[i][j]);
//        methodVisitor.visitInsn(AASTORE);
//      }
//      methodVisitor.visitInsn(ARETURN);
//    }
//    methodVisitor.visitLabel(defaultLabel);
//    loadInteger(methodVisitor, 0);
//    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
//    methodVisitor.visitInsn(ARETURN);
//    methodVisitor.visitMaxs(0, 0);
//    methodVisitor.visitEnd();
  }


  private void generateAugmentationBytecode(final GoloModule module, final String target, final Set<GoloFunction> functions) {
    NotYetImplemented.t();
//    ClassWriter mainClassWriter = classWriter;
//    String mangledClass = target.replace('.', '$');
//    PackageAndClass packageAndClass = new PackageAndClass(
//        module.getPackageAndClass().packageName(),
//        module.getPackageAndClass().className() + "$" + mangledClass);
//    String augmentationClassInternalName = packageAndClass.toJVMType();
//    String outerName = module.getPackageAndClass().toJVMType();
//
//    mainClassWriter.visitInnerClass(
//        augmentationClassInternalName,
//        outerName,
//        mangledClass,
//        ACC_PUBLIC | ACC_STATIC);
//
//    classWriter = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
//    classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, augmentationClassInternalName, null, JOBJECT, null);
//    classWriter.visitSource(sourceFilename, null);
//    classWriter.visitOuterClass(outerName, null, null);
//
//    for (GoloFunction function : functions) {
//      function.accept(this);
//    }
//
//    Set<ModuleImport> imports = new HashSet<>(module.getImports());
//    imports.add(new ModuleImport(module.getPackageAndClass()));
//    writeImportMetaData(imports);
//
//    classWriter.visitEnd();
//    generationResults.add(new CodeGenerationResult(classWriter.toByteArray(), packageAndClass));
//    classWriter = mainClassWriter;
  }

  public Function visitFunction(final GoloFunction function) {
    FrameDescriptor frameDesc = new FrameDescriptor(null);
    context.frameDescriptors.push(frameDesc);


    // TODO: we should assemble a runtime representation here
//    int accessFlags = (function.getVisibility() == PUBLIC) ? ACC_PUBLIC : ACC_PRIVATE;
//    String signature;
//    if (function.isMain()) {
//      signature = "([Ljava/lang/String;)V";
//    } else if (function.isVarargs()) {
//      accessFlags = accessFlags | ACC_VARARGS;
//      signature = goloVarargsFunctionSignature(function.getArity());
//    } else if (function.isModuleInit()) {
//      signature = "()V";
//    } else {
//      signature = goloFunctionSignature(function.getArity());
//    }
//    if (function.isSynthetic() || function.isDecorator()) {
//      accessFlags = accessFlags | ACC_SYNTHETIC;
//    }
//    function.getName(),

    if (function.isDecorated()) {
      // TODO: decorations...
      NotYetImplemented.t();

//      AnnotationVisitor annotation = methodVisitor.visitAnnotation("Lgololang/annotations/DecoratedBy;", true);
//      annotation.visit("value", function.getDecoratorRef());
//      annotation.visitEnd();
    }

//    for(String parameter: function.getParameterNames()) {
//      methodVisitor.visitParameter(parameter, ACC_FINAL);
//    }
//    methodVisitor.visitCode();

    ExpressionNode body = function.getBlock().accept(this);
    context.frameDescriptors.pop();
    return new Function(body, function, frameDesc);
  }

  public void visitDecorator(final Decorator decorator) {
    decorator.getExpressionStatement().accept(this);
  }

  public ExpressionNode visitBlock(final Block block) {
    ReferenceTable referenceTable = block.getReferenceTable();
    context.referenceTableStack.push(referenceTable);

    List<GoloStatement> irStatements = block.getStatements();
    List<ExpressionNode> statements = new ArrayList<ExpressionNode>(irStatements.size());
    for (GoloStatement statement : irStatements) {
      statements.add((ExpressionNode) statement.accept(this));
    }

    for (LocalReference localReference : referenceTable.ownedReferences()) {
      if (localReference.isModuleState()) {
        continue;
      }
    }

    context.referenceTableStack.pop();

    return createSequence(statements);
  }

  private ExpressionNode createSequence(final List<ExpressionNode> statements) {
    if (statements.size() == 0) {
      return new NullLiteralNode();
    }
    if (statements.size() == 1) {
      return statements.get(0);
    }
    return new SequenceNode(statements.toArray(new ExpressionNode[statements.size()]));
  }

  private boolean isMethodCall(final BinaryOperation operation) {
    return operation.getType() == METHOD_CALL
            || operation.getType() == ELVIS_METHOD_CALL
            || operation.getType() == ANON_CALL;
  }

  public LiteralNode visitConstantStatement(final ConstantStatement constantStatement) {
    Object value = constantStatement.getValue();
    if (value == null) {
      return new NullLiteralNode();
    }
    if (value instanceof Integer) {
      return new IntegerLiteralNode((Integer) value);
    }
    if (value instanceof Long) {
      return new LongLiteralNode((Long) value);
    }
    if (value instanceof Boolean) {
      if ((Boolean) value) {
        return new TrueLiteralNode();
      } else {
        return new FalseLiteralNode();
      }
    }
    if (value instanceof String) {
      return new StringLiteralNode((String) value);
    }
    if (value instanceof Character) {
      return new CharacterLiteralNode((Character) value);
    }

    // TODO: figure out what exactly is happening here. At runtime, do we need some form of GoloClass object?
    //       or is the ParserClassRef sufficient for Truffle? Probably not, because we can't do anything with it
    if (value instanceof GoloParser.ParserClassRef) {
      NotYetImplemented.t();
//      GoloParser.ParserClassRef ref = (GoloParser.ParserClassRef) value;
//      methodVisitor.visitInvokeDynamicInsn(ref.name.replaceAll("\\.", "#"), "()Ljava/lang/Class;", CLASSREF_HANDLE);
//      return;
    }

    // TODO: same as with ClassRef, we probably need some proper function object (which, also probably should either
    //       be, or at least contain a Truffle RootNode
    if (value instanceof GoloParser.FunctionRef) {
      NotYetImplemented.t();
//      GoloParser.FunctionRef ref = (GoloParser.FunctionRef) value;
//      String module = ref.module;
//      if (module == null) {
//        module = klass;
//      }
//      methodVisitor.visitLdcInsn(ref.name);
//      methodVisitor.visitInvokeDynamicInsn(module.replaceAll("\\.", "#"), "()Ljava/lang/Class;", CLASSREF_HANDLE);
//      methodVisitor.visitInvokeDynamicInsn(
//          "gololang#Predefined#fun",
//          "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
//          FUNCTION_INVOCATION_HANDLE,
//          (Object) 1); // this specific call can be banged
//      return;
    }
    if (value instanceof Double) {
      return new DoubleLiteralNode((Double) value);
    }
    if (value instanceof Float) {
      return new FloatLiteralNode((Float) value);
    }
    throw new IllegalArgumentException("Constants of type " + value.getClass() + " cannot be handled.");
  }

  public ExpressionNode visitReturnStatement(final ReturnStatement returnStatement) {
    // TODO: ok, if we are at the end of a function, we don't want a return exception
    //       only and only if we are really in some nested expression, we need to throw an exception
    //       otherwise, we can always just return here the expression statement directly
    return new ReturnNode((ExpressionNode) returnStatement.getExpressionStatement().accept(this));

    // TODO: special treatment for void
//    if (returnStatement.isReturningVoid()) {
//      methodVisitor.visitInsn(RETURN);
//    } else {
//      methodVisitor.visitInsn(ARETURN);
//    }

  }

  public ThrowNode visitThrowStatement(final ThrowStatement throwStatement) {
    return ThrowNodeGen.create((ExpressionNode) throwStatement.getExpressionStatement().accept(this));
  }

  private ExpressionNode[] visitInvocationArguments(final AbstractInvocation invocation) {
    List<ExpressionStatement> argStatements = invocation.getArguments();
    ExpressionNode[] argumentNodes = new ExpressionNode[argStatements.size()];
    int i = 0;
    for (ExpressionStatement argument : argStatements) {
      if (invocation.usesNamedArguments()) {
        NotYetImplemented.t();
//        NamedArgument namedArgument = (NamedArgument) argument;
//        argumentNames.add(namedArgument.getName());
//        argument = namedArgument.getExpression();
      }
      argumentNodes[i] = (ExpressionNode) argument.accept(this);
      i++;
    }
    return argumentNodes;
  }

  public ExpressionNode visitFunctionInvocation(final FunctionInvocation functionInvocation) {
//    String name = functionInvocation.getName().replaceAll("\\.", "#");
//    String typeDef = goloFunctionSignature(functionInvocation.getArity());
//    Handle handle = FUNCTION_INVOCATION_HANDLE;
    if (functionInvocation.isConstant()) {
      NotYetImplemented.t(); // This is for literal annonoumous functions, I think, can probably have a separate node
    }

    List<Object> bootstrapArgs = new ArrayList<>();

    if (functionInvocation.isOnReference()) {
      NotYetImplemented.t();
//      ReferenceTable table = context.referenceTableStack.peek();
//
//      // -->> this is probably a local variable read, will need a lot object here
//      methodVisitor.visitVarInsn(ALOAD, table.get(functionInvocation.getName()).getIndex());
    }
    if (functionInvocation.isOnModuleState()) {
      NotYetImplemented.t();
//      visitReferenceLookup(new ReferenceLookup(functionInvocation.getName()));
    }
    if (functionInvocation.isAnonymous() || functionInvocation.isOnReference() || functionInvocation.isOnModuleState()) {
      NotYetImplemented.t();
//      methodVisitor.visitTypeInsn(CHECKCAST, "gololang/FunctionReference");
//      MethodType type = genericMethodType(functionInvocation.getArity() + 1).changeParameterType(0, FunctionReference.class);
//      typeDef = type.toMethodDescriptorString();
//      handle = CLOSURE_INVOCATION_HANDLE;
    }
    ExpressionNode[] arguments = visitInvocationArguments(functionInvocation);

//    bootstrapArgs.addAll(argumentNames);
//    methodVisitor.visitInvokeDynamicInsn(name, typeDef, handle, bootstrapArgs.toArray());

    for (FunctionInvocation invocation : functionInvocation.getAnonymousFunctionInvocations()) {
      NotYetImplemented.t();
      invocation.accept(this);
    }

    // Could also be a ClosureInvocationNode, see one of the NYI branches. and earlier isConstant()
    return FunctionInvocationNode.create(functionInvocation.getName(), module, new EvalArgumentsNode(arguments));
  }

  public ExpressionNode visitMethodInvocation(final MethodInvocation methodInvocation) {
    throw new NotYetImplemented();
//    List<Object> bootstrapArgs = new ArrayList<>();
//    bootstrapArgs.add(methodInvocation.isNullSafeGuarded() ? 1 : 0);
//    List<String> argumentNames = visitInvocationArguments(methodInvocation);
//    bootstrapArgs.addAll(argumentNames);
//    methodVisitor.visitInvokeDynamicInsn(
//        methodInvocation.getName().replaceAll("\\.", "#"),
//        goloFunctionSignature(methodInvocation.getArity() + 1),
//        METHOD_INVOCATION_HANDLE,
//        bootstrapArgs.toArray());
//    for (FunctionInvocation invocation : methodInvocation.getAnonymousFunctionInvocations()) {
//      invocation.accept(this);
//    }
  }

  public ExpressionNode visitAssignmentStatement(final AssignmentStatement assignmentStatement) {
    LocalReference reference = assignmentStatement.getLocalReference();
    if (reference.isModuleState()) {
      throw new NotYetImplemented();
//      methodVisitor.visitInvokeDynamicInsn(
//          (klass + "." + reference.getName()).replaceAll("\\.", "#"),
//          "(Ljava/lang/Object;)V",
//          FUNCTION_INVOCATION_HANDLE,
//          (Object) 0);
    } else {
      FrameSlot slot = getFrameSlot(reference);
      return LocalVariableWriteNodeGen.create(
          slot, (ExpressionNode) assignmentStatement.getExpressionStatement().accept(this));
    }
  }

  private FrameSlot getFrameSlot(final LocalReference reference) {
    // TODO: might want to specialize this later on
    FrameSlot slot = context.frameDescriptors.peek().findOrAddFrameSlot(reference.getName(), FrameSlotKind.Object);
    return slot;
  }

  public ExpressionNode visitReferenceLookup(final ReferenceLookup referenceLookup) {
    LocalReference reference = referenceLookup.resolveIn(context.referenceTableStack.peek());
    if (reference.isModuleState()) {
      throw new NotYetImplemented();
//      methodVisitor.visitInvokeDynamicInsn(
//          (klass + "." + referenceLookup.getName()).replaceAll("\\.", "#"),
//          "()Ljava/lang/Object;",
//          FUNCTION_INVOCATION_HANDLE,
//          (Object) 0);
    } else if (reference.isArgument()) {
      return new LocalArgumentReadNode(reference.getIndex());
    } else {
      FrameSlot slot = getFrameSlot(reference);
      return new LocalVariableReadNode(slot);
    }
  }

  public ExpressionNode visitConditionalBranching(final ConditionalBranching conditionalBranching) {
    ExpressionNode condition = (ExpressionNode) conditionalBranching.getCondition().accept(this);
    ExpressionNode thenNode  = conditionalBranching.getTrueBlock().accept(this);
    ExpressionNode elseNode;

    if (conditionalBranching.hasFalseBlock()) {
      elseNode = conditionalBranching.getFalseBlock().accept(this);
    } else if (conditionalBranching.hasElseConditionalBranching()) {
      elseNode = (ExpressionNode) conditionalBranching.getElseConditionalBranching().accept(this);
    } else {
      elseNode = null;
    }
    return new IfNode(condition, thenNode, elseNode);
  }

  public ExpressionNode visitLoopStatement(final LoopStatement loopStatement) {
    // TODO: it seems like the loops do not have their own scope. if they do, we should adapt the loop node to use a separate frame
    ExpressionNode init;
    if (loopStatement.hasInitStatement()) {
      init = (ExpressionNode) loopStatement.getInitStatement().accept(this);
    } else {
      init = null;
    }

    ExpressionNode condition = (ExpressionNode) loopStatement.getConditionStatement().accept(this);
    ExpressionNode body      = loopStatement.getBlock().accept(this);

    ExpressionNode post;
    if (loopStatement.hasPostStatement()) {
      post = (ExpressionNode) loopStatement.getPostStatement().accept(this);
    } else {
      post = null;
    }
    return new ForLoopNode(init, condition, body, post);
  }

  public ExpressionNode visitLoopBreakFlowStatement(final LoopBreakFlowStatement loopBreakFlowStatement) {
    throw new NotYetImplemented();
//    Label jumpTarget;
//    if (LoopBreakFlowStatement.Type.BREAK.equals(loopBreakFlowStatement.getType())) {
//      jumpTarget = context.loopEndMap.get(loopBreakFlowStatement.getEnclosingLoop());
//    } else {
//      jumpTarget = context.loopStartMap.get(loopBreakFlowStatement.getEnclosingLoop());
//    }
//    methodVisitor.visitLdcInsn(0);
//    methodVisitor.visitJumpInsn(IFEQ, jumpTarget);
//    // NOP + ATHROW invalid frames if the GOTO is followed by an else branch code...
//    // methodVisitor.visitJumpInsn(GOTO, jumpTarget);
  }

  public ExpressionNode visitCollectionLiteral(final CollectionLiteral collectionLiteral) {
    throw new NotYetImplemented();
//    switch (collectionLiteral.getType()) {
//      case tuple:
//        createTuple(collectionLiteral);
//        break;
//      case array:
//        createArray(collectionLiteral);
//        break;
//      case list:
//        createList(collectionLiteral);
//        break;
//      case vector:
//        createVector(collectionLiteral);
//        break;
//      case set:
//        createSet(collectionLiteral);
//        break;
//      case map:
//        createMap(collectionLiteral);
//        break;
//      default:
//        throw new UnsupportedOperationException("Can't handle collections of type " + collectionLiteral.getType() + " yet");
//    }
  }

  private void createMap(final CollectionLiteral collectionLiteral) {
    methodVisitor.visitTypeInsn(NEW, "java/util/LinkedHashMap");
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "()V", false);
    for (ExpressionStatement expression : collectionLiteral.getExpressions()) {
      methodVisitor.visitInsn(DUP);
      expression.accept(this);
      methodVisitor.visitTypeInsn(CHECKCAST, "gololang/Tuple");
      methodVisitor.visitInsn(DUP);
      loadInteger(methodVisitor, 0);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "gololang/Tuple", "get", "(I)Ljava/lang/Object;", false);
      methodVisitor.visitInsn(SWAP);
      loadInteger(methodVisitor, 1);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "gololang/Tuple", "get", "(I)Ljava/lang/Object;", false);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/LinkedHashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
      methodVisitor.visitInsn(POP);
    }
  }

  private void createSet(final CollectionLiteral collectionLiteral) {
    methodVisitor.visitTypeInsn(NEW, "java/util/LinkedHashSet");
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedHashSet", "<init>", "()V", false);
    for (ExpressionStatement expression : collectionLiteral.getExpressions()) {
      methodVisitor.visitInsn(DUP);
      expression.accept(this);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/LinkedHashSet", "add", "(Ljava/lang/Object;)Z", false);
      methodVisitor.visitInsn(POP);
    }
  }

  private void createVector(final CollectionLiteral collectionLiteral) {
    methodVisitor.visitTypeInsn(NEW, "java/util/ArrayList");
    methodVisitor.visitInsn(DUP);
    loadInteger(methodVisitor, collectionLiteral.getExpressions().size());
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V", false);
    for (ExpressionStatement expression : collectionLiteral.getExpressions()) {
      methodVisitor.visitInsn(DUP);
      expression.accept(this);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
      methodVisitor.visitInsn(POP);
    }
  }

  private void createList(final CollectionLiteral collectionLiteral) {
    methodVisitor.visitTypeInsn(NEW, "java/util/LinkedList");
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false);
    for (ExpressionStatement expression : collectionLiteral.getExpressions()) {
      methodVisitor.visitInsn(DUP);
      expression.accept(this);
      methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/LinkedList", "add", "(Ljava/lang/Object;)Z", false);
      methodVisitor.visitInsn(POP);
    }
  }

  private void createArray(final CollectionLiteral collectionLiteral) {
    loadInteger(methodVisitor, collectionLiteral.getExpressions().size());
    methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    int i = 0;
    for (ExpressionStatement expression : collectionLiteral.getExpressions()) {
      methodVisitor.visitInsn(DUP);
      loadInteger(methodVisitor, i);
      expression.accept(this);
      methodVisitor.visitInsn(AASTORE);
      i = i + 1;
    }
  }

  private void createTuple(final CollectionLiteral collectionLiteral) {
    methodVisitor.visitTypeInsn(NEW, "gololang/Tuple");
    methodVisitor.visitInsn(DUP);
    createArray(collectionLiteral);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "gololang/Tuple", "<init>", "([Ljava/lang/Object;)V", false);
  }

  public ExpressionNode visitTryCatchFinally(final TryCatchFinally tryCatchFinally) {
    throw new NotYetImplemented();
//    Label tryStart = new Label();
//    Label tryEnd = new Label();
//    Label catchStart = new Label();
//    Label catchEnd = new Label();
//
//    Label rethrowStart = null;
//    Label rethrowEnd = null;
//    if (tryCatchFinally.isTryCatchFinally()) {
//      rethrowStart = new Label();
//      rethrowEnd = new Label();
//    }
//
//    methodVisitor.visitLabel(tryStart);
//    tryCatchFinally.getTryBlock().accept(this);
//    if (tryCatchFinally.isTryCatch() || tryCatchFinally.isTryCatchFinally()) {
//      methodVisitor.visitJumpInsn(GOTO, catchEnd);
//    }
//    methodVisitor.visitTryCatchBlock(tryStart, tryEnd, catchStart, null);
//    methodVisitor.visitLabel(tryEnd);
//
//    if (tryCatchFinally.isTryFinally()) {
//      tryCatchFinally.getFinallyBlock().accept(this);
//      methodVisitor.visitJumpInsn(GOTO, catchEnd);
//    }
//
//    if (tryCatchFinally.isTryCatchFinally()) {
//      methodVisitor.visitTryCatchBlock(catchStart, catchEnd, rethrowStart, null);
//    }
//
//    methodVisitor.visitLabel(catchStart);
//    if (tryCatchFinally.isTryCatch() || tryCatchFinally.isTryCatchFinally()) {
//      Block catchBlock = tryCatchFinally.getCatchBlock();
//      int exceptionRefIndex = catchBlock.getReferenceTable().get(tryCatchFinally.getExceptionId()).getIndex();
//      methodVisitor.visitVarInsn(ASTORE, exceptionRefIndex);
//      tryCatchFinally.getCatchBlock().accept(this);
//    } else {
//      tryCatchFinally.getFinallyBlock().accept(this);
//      methodVisitor.visitInsn(ATHROW);
//    }
//    methodVisitor.visitLabel(catchEnd);
//
//    if (tryCatchFinally.isTryCatchFinally()) {
//      tryCatchFinally.getFinallyBlock().accept(this);
//      methodVisitor.visitJumpInsn(GOTO, rethrowEnd);
//      methodVisitor.visitLabel(rethrowStart);
//      tryCatchFinally.getFinallyBlock().accept(this);
//      methodVisitor.visitInsn(ATHROW);
//      methodVisitor.visitLabel(rethrowEnd);
//    }
  }

  public ExpressionNode visitClosureReference(final ClosureReference closureReference) {
    throw new NotYetImplemented();
//    GoloFunction target = closureReference.getTarget();
//    final boolean isVarArgs = target.isVarargs();
//    final int arity = (isVarArgs) ? target.getArity() - 1 : target.getArity();
//    final int syntheticCount = closureReference.getTarget().getSyntheticParameterCount();
//    methodVisitor.visitInvokeDynamicInsn(
//        target.getName(),
//        methodType(FunctionReference.class).toMethodDescriptorString(),
//        CLOSUREREF_HANDLE,
//        klass,
//        (Integer) arity,
//        (Boolean) isVarArgs);
//    if (syntheticCount > 0) {
//      String[] refs = closureReference.getCapturedReferenceNames().toArray(new String[syntheticCount]);
//      loadInteger(methodVisitor, 0);
//      loadInteger(methodVisitor, syntheticCount);
//      methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
//      ReferenceTable table = context.referenceTableStack.peek();
//      for (int i = 0; i < syntheticCount; i++) {
//        methodVisitor.visitInsn(DUP);
//        loadInteger(methodVisitor, i);
//        methodVisitor.visitVarInsn(ALOAD, table.get(refs[i]).getIndex());
//        methodVisitor.visitInsn(AASTORE);
//      }
//      methodVisitor.visitMethodInsn(
//          INVOKEVIRTUAL,
//          "gololang/FunctionReference",
//          "insertArguments",
//          "(I[Ljava/lang/Object;)Lgololang/FunctionReference;", false);
//      if (isVarArgs) {
//        methodVisitor.visitLdcInsn(Type.getType(Object[].class));
//        methodVisitor.visitMethodInsn(
//            INVOKEVIRTUAL,
//            "gololang/FunctionReference",
//            "asVarargsCollector",
//            "(Ljava/lang/Class;)Lgololang/FunctionReference;", false);
//      }
//    }
  }

  public BinaryNode visitBinaryOperation(final BinaryOperation binaryOperation) {
    OperatorType operatorType = binaryOperation.getType();
    if (AND.equals(operatorType)) {
      throw new NotYetImplemented();
      // return andOperator(binaryOperation);
    } else if (OR.equals(operatorType)) {
      throw new NotYetImplemented();
//      return orOperator(binaryOperation);
    } else {
      return genericBinaryOperator(binaryOperation, operatorType);
    }
  }

  private BinaryNode genericBinaryOperator(
      final BinaryOperation binaryOperation, final OperatorType operatorType) {
    if (!isMethodCall(binaryOperation)) {
      return operatorType.createNode(
          (ExpressionNode) binaryOperation.getLeftExpression().accept(this),
          (ExpressionNode) binaryOperation.getRightExpression().accept(this));
    } else {
      assert false;
      throw new NotYetImplemented();
    }
  }

  public ExpressionNode visitUnaryOperation(final UnaryOperation unaryOperation) {
    throw new NotYetImplemented();
//    String name = unaryOperation.getType().name().toLowerCase();
//    unaryOperation.getExpressionStatement().accept(this);
//    methodVisitor.visitInvokeDynamicInsn(name, goloFunctionSignature(1), OPERATOR_HANDLE, (Integer) 1);
  }
}

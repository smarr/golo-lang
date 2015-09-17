/*
 * Copyright (c) 2012-2015 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package fr.insalyon.citi.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.oracle.truffle.api.Truffle;

import fr.insalyon.citi.golo.cli.command.spi.CliCommand;
import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.GoloCompiler;
import gololang.truffle.Function;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

@Parameters(commandNames = {"golo"}, commandDescription = "Dynamically loads and runs from Golo source files")
public class GoloGoloCommand implements CliCommand {

  @Parameter(names = "--files", variableArity = true, description = "Golo source files (*.golo and directories). The last one has a main function or use --module", required = true)
  List<String> files = new LinkedList<>();

  @Parameter(names = "--module", description = "The Golo module with a main function")
  String module;

  @Parameter(names = "--args", variableArity = true, description = "Program arguments")
  List<String> arguments = new LinkedList<>();

  @Parameter(names = "--classpath", variableArity = true, description = "Classpath elements (.jar and directories)")
  List<String> classpath = new LinkedList<>();

  @Parameter(names = "--truffle", description = "Uses Truffle for execution")
  boolean truffle = false;

  public void execute() throws Throwable {
    URLClassLoader primaryClassLoader = primaryClassLoader(this.classpath);
    Thread.currentThread().setContextClassLoader(primaryClassLoader);
    GoloClassLoader loader = new GoloClassLoader(primaryClassLoader);
    Class<?> lastClass = null;
    for (String goloFile : this.files) {
      lastClass = loadGoloFile(goloFile, this.module, loader);
    }
    if (lastClass == null && this.module != null) {
      System.out.println("The module " + this.module + " does not exist in the classpath.");
      return;
    }
    callRun(lastClass, this.arguments.toArray(new String[this.arguments.size()]));
  }

  private Class<?> loadGoloFile(String goloFile, String module, GoloClassLoader loader) throws Throwable {
    File file = new File(goloFile);
    if (!file.exists()) {
      System.out.println("Error: " + file.getAbsolutePath() + " does not exist.");
    } else if (file.isDirectory()) {
      File[] directoryFiles = file.listFiles();
      if (directoryFiles != null) {
        Class<?> lastClass = null;
        for (File directoryFile : directoryFiles) {
          Class<?> loadedClass = loadGoloFile(directoryFile.getAbsolutePath(), module, loader);
          if (module == null || (loadedClass != null && loadedClass.getCanonicalName().equals(module))) {
            lastClass = loadedClass;
          }
        }
        return lastClass;
      }
    } else if (file.getName().endsWith(".golo")) {
      try (FileInputStream in = new FileInputStream(file)) {
        if (truffle) {
          GoloCompiler compiler = new GoloCompiler();
          Function fun = compiler.compileAndGetMain(file.getName(), in);
	      Truffle.getRuntime().createCallTarget(fun).call(new Object[] {new String[0]});
	      System.exit(0);  // we want to exit here, is probably not totally correct, but we don't want the rest of the system to run
        } else {
          Class<?> loadedClass = loader.load(file.getName(), in);
          if (module == null || loadedClass.getCanonicalName().equals(module)) {
            return loadedClass;
          }
        }
      } catch (GoloCompilationException e) {
        handleCompilationException(e);
      }
    }
    return null;
  }
}

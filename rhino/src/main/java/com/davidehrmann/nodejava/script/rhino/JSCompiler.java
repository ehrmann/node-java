/*
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *    Bob Jervis
 *    David Ehrmann
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 */

package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Evaluator;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.optimizer.Codegen;

import java.io.IOException;
import java.io.Reader;

public class JSCompiler {

    public static String getClassName(String jsFile) {

        // Convert the path name to a class name.
        // This is slightly modified logic from the Rhino code.  The Rhino code appends a
        // sequence ID to the class name
        String baseName = "c";
        if (jsFile.length() > 0) {
            baseName = jsFile.replaceAll("\\W", "_");
            if (!Character.isJavaIdentifierStart(baseName.charAt(0))) {
                baseName = "_" + baseName;
            }
        }

        return "org.mozilla.javascript.gen." + baseName;
    }

    // This is a modified version of org.mozilla.javascript.Context.compileImpl()
    public CompiledClass compileImpl(Context cx, Reader sourceReader, String sourceName) throws IOException {

        if (sourceName == null) {
            throw new NullPointerException("sourceName was null");
        }

        CompilerEnvirons compilerEnv = new CompilerEnvirons();
        compilerEnv.initFromContext(cx);

        ErrorReporter compilationErrorReporter = new ErrorReporter() {

            public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
                // TODO Auto-generated method stub

            }

            public EvaluatorException runtimeError(String arg0, String arg1, int arg2, String arg3, int arg4) {
                // TODO Auto-generated method stub
                return null;
            }

            public void warning(String arg0, String arg1, int arg2, String arg3, int arg4) {
                // TODO Auto-generated method stub

            }
        };

        Parser p = new Parser(compilerEnv, compilationErrorReporter);
        AstRoot tree = p.parse(sourceReader, sourceName, 1);
        Evaluator compiler = new CustomCodegen();
        Object bytecode = compiler.compile(compilerEnv, tree, tree.getEncodedSource(), false);
        return (CompiledClass) bytecode;
    }

    public static class CompilationException extends Exception {

        private static final long serialVersionUID = 9218214937234464108L;

        public CompilationException(String message) {
            super(message);
        }
    }

    protected static class CustomCodegen extends Codegen {

        @Override
        public Object compile(CompilerEnvirons compilerEnv, ScriptNode tree, String encodedSource, boolean returnFunction) {
            String mainClassName = getClassName(tree.getSourceName());
            byte[] mainClassBytes = compileToClassFile(compilerEnv, mainClassName, tree, encodedSource, returnFunction);
            return new CompiledClass(mainClassName, mainClassBytes);
        }
    }

    public static class CompiledClass {

        public final String className;
        public final byte[] classBytes;

        public CompiledClass(String className, byte[] classBytes) {
            if (className == null) {
                throw new NullPointerException("className was null");
            }
            if (classBytes == null) {
                throw new NullPointerException("classBytes was null");
            }

            this.className = className;
            this.classBytes = classBytes;
        }
    }
}

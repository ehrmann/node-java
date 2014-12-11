package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.runtime.Console;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptCompiler;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptRunner;
import com.davidehrmann.nodejava.scriptloader.ClasspathScriptLoader;
import com.davidehrmann.nodejava.scriptloader.MemoizedResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ScriptLoader;
import org.junit.Test;
import org.mozilla.javascript.Script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ModuleTest {

    @Test
    public void testRecursiveRequire() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (PrintStream printStream = new PrintStream(out, true, "ASCII")) {
                ScriptLoader scriptLoader = new ClasspathScriptLoader(this.getClass().getClassLoader());
                ScriptCompiler<Script> compiler = new RhinoScriptCompiler();
                ResolvingScriptLoader<Script> resolvingScriptLoader = new MemoizedResolvingScriptLoader<>(scriptLoader, SimplePathResolutionStrategy.INSTANCE, compiler);
                Console console = new Console(printStream, printStream);
                String path = Util.getPathOfClass(this);

                ResolvingScriptLoader.ResolvedScript script = resolvingScriptLoader.loadScript("main.js", path);
                Module<Object, Script> main = new Module<>(Collections.<String, Object>singletonMap("console", console), new RhinoScriptRunner(), resolvingScriptLoader, script);
                main.run();
            }

            assertEquals(
                    Arrays.asList(new String[]{
                            "main starting",
                            "a starting",
                            "b starting",
                            "in b, a.done = false",
                            "b done",
                            "in a, b.done = true",
                            "a done",
                            "in main, a.done=true, b.done=true",
                    }),
                    Arrays.asList(new String(out.toByteArray(), "ASCII").split("\\r?\\n"))
            );
        }
    }
}

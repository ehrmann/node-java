package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.script.CompilationException;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.util.PaddingReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class RhinoScriptCompiler implements ScriptCompiler<Script> {
    protected static final char[] MATCH_PREFIX = "#!".toCharArray();
    protected static final char[] HEADER = "(function() { // ".toCharArray();
    protected static final char[] TRAILER = "\n})();".toCharArray();

    @Override
    public Script compile(InputStream in, String absolutePath) throws CompilationException {
        try (InputStream bufferedIn = new BufferedInputStream(in);
             Reader reader = new InputStreamReader(bufferedIn, "UTF-8");
             PaddingReader paddingReader = new PaddingReader(reader, HEADER, TRAILER, MATCH_PREFIX)
        ) {
            Context cx = Context.enter();
            try {
                return cx.compileReader(paddingReader, absolutePath, 1, null);
            } finally {
                Context.exit();
            }
        } catch (IOException e) {
            throw new CompilationException(e);
        }
    }
}
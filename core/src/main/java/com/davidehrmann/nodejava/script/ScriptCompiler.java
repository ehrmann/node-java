package com.davidehrmann.nodejava.script;

import java.io.InputStream;

public interface ScriptCompiler<S> {
    S compile(InputStream in, String absolutePath) throws CompilationException;
}
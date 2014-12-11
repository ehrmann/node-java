package com.davidehrmann.nodejava.scriptloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * ChainedScriptLoader is a ScriptLoader that's used for searching different paths.
 * These can be any paths there's a ScriptLoader for--FS paths, the Java clasapth, a DB-backed path, etc.
 */
public class ChainedScriptLoader implements ScriptLoader {

    protected final List<ScriptLoader> scriptLoaders;

    public ChainedScriptLoader(List<ScriptLoader> scriptLoaders) {
        this.scriptLoaders = Objects.requireNonNull(scriptLoaders, "scriptLoaders was null");
    }

    public InputStream loadScript(String canonicalPath) throws IOException {
        for (ScriptLoader scriptLoader : this.scriptLoaders) {
            try {
                return scriptLoader.loadScript(canonicalPath);
            } catch (FileNotFoundException e) {
                // Try the next ScriptLoader
            }
        }

        throw new FileNotFoundException("Couldn't find file'" + canonicalPath + "'");
    }
}

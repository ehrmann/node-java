package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.script.CompilationException;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.scriptloader.ScriptLoader;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * ClassLoaderScriptLoader combines a ScriptLoader and a ScriptCompiler to load precompiled scripts from the classpath
 *
 * @param <S> The script class this ScriptCompiler generates
 */
public class RhinoClassLoaderScriptLoader<S> implements ScriptLoader, ScriptCompiler<S> {

    protected final ClassLoader classLoader;
    protected final ScriptCompiler<S> compiler;
    protected final Class<S> scriptClass;

    public RhinoClassLoaderScriptLoader(ClassLoader classLoader, ScriptCompiler<S> compiler, Class<S> scriptClass) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader was null");
        this.compiler = Objects.requireNonNull(compiler, "compiler was null");
        this.scriptClass = Objects.requireNonNull(scriptClass, "scriptClass was null");
    }

    public InputStream loadScript(String canonicalPath) {
        // Trim out the leading forward slash
        canonicalPath = canonicalPath.substring(1);

        // Now try to load and instantiate the class
        try {
            // TODO: this translation depends on the compiler
            Class<?> jsClass = Class.forName(JSCompiler.getClassName(canonicalPath));
            if (scriptClass.isAssignableFrom(jsClass)) {
                @SuppressWarnings("unchecked")
                Class<S> jsScriptClass = (Class<S>) jsClass;
                InputStream in = new ByteArrayInputStream("// placeholder; script loaded as java class\n".getBytes("ASCII"));
                return new PlaceholderInputStream<S>(this, in, jsScriptClass.newInstance());
            }
        } catch (ClassNotFoundException e) {
            // The path resolver might be trying different things, so this error could be benign
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO: this is a real error; the class is very much a Rhino JS class, but it's not acting like one.
            // TODO: maybe this is a compilation error
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public S compile(InputStream in, String absolutePath) throws CompilationException {
        if (in instanceof PlaceholderInputStream) {
            @SuppressWarnings("unchecked")
            PlaceholderInputStream<S> placeholderInputStream = (PlaceholderInputStream<S>) in;
            if (placeholderInputStream.scriptLoader != this) {
                throw new IllegalArgumentException("InputStream wasn't created by " + this.getClass().getSimpleName());
            }

            return placeholderInputStream.script;
        }

        return this.compiler.compile(in, absolutePath);
    }

    private static class PlaceholderInputStream<S> extends FilterInputStream {
        private final S script;
        private final RhinoClassLoaderScriptLoader<S> scriptLoader;

        protected PlaceholderInputStream(RhinoClassLoaderScriptLoader<S> scriptLoader, InputStream in, S script) {
            super(in);
            this.script = Objects.requireNonNull(script);
            this.scriptLoader = Objects.requireNonNull(scriptLoader);
        }
    }
}

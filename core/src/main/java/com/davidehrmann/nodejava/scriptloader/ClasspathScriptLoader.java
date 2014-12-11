package com.davidehrmann.nodejava.scriptloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClasspathScriptLoader implements ScriptLoader {

    protected final ClassLoader classLoader;

    public ClasspathScriptLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader was null");
        }

        this.classLoader = classLoader;
    }

    public InputStream loadScript(String canonicalPath) throws IOException {
        if (canonicalPath.length() == 0) {
            throw new IOException("Empty absolutePath");
        } else if (canonicalPath.length() == 1) {
            throw new IOException("No file in absolutePath");
        } else {
            // Trip the leading forward slash from the absolute path
            canonicalPath = canonicalPath.substring(1);

            // Open the resource and return the stream
            InputStream in = this.classLoader.getResourceAsStream(canonicalPath);

            if (in == null) {
                throw new FileNotFoundException("Couldn't find file '" + canonicalPath + "' on the classpath");
            }

            return in;
        }
    }

}

package com.davidehrmann.nodejava.runtime;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RuntimeBuilder {

    protected final Map<String, Object> map = new LinkedHashMap<>();

    public RuntimeBuilder() {
        // TODO: IO reactor injection

        map.put("console", new Console(System.out, System.err));
        map.put("path", new Path());
        map.put("fs", new FS());
        map.put("url", new Url());
        map.put("os", new Os());
        map.put("sys", new Util());
        map.put("path", new Path());
        map.put("util", new Util());
        map.put("process", new Process(new String[0]));
    }

    public RuntimeBuilder withConsole(InputStream stdin, PrintStream stdout, PrintStream stderr) {
        map.put("console", new Console(stdout, stderr));
        return this;
    }

    public RuntimeBuilder withProcess(String[] args) {
        map.put("process", new Process(args));
        return this;
    }

    public Map<String, Object> build() {
        // TODO: construct IO reactor
        return Collections.unmodifiableMap(map);
    }
}

package com.davidehrmann.nodejava.runtime;

import java.io.File;
import java.io.IOException;

public class Process {

    public String[] argv;

    public Process(String[] argv) {
        this.argv = argv;
    }

    public void on(Object event, Object callback) {
        System.out.println("");
        // Function
    }

    @NodeProperty("argv")
    public String[] getArgv() {
        return this.argv;
    }

    @NodeProperty
    public String cwd() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

}

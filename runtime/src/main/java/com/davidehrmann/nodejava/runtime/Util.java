package com.davidehrmann.nodejava.runtime;

public class Util {

    @NodeFunction
    public void print(String s) {
        System.out.print(s);
    }

    @NodeFunction
    public void puts(String s) {
        System.out.println(s);
    }

}

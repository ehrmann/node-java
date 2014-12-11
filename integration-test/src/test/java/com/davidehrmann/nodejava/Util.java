package com.davidehrmann.nodejava;

public class Util {

    public static String getPathOfClass(Object thiz) {
        return "/" + thiz.getClass().getCanonicalName().replace('.', '/').replaceFirst("/[^/]+$", "");
    }

    public static String getPathOfClass(Class<?> clazz) {
        return "/" + clazz.getCanonicalName().replace('.', '/').replaceFirst("/[^/]+$", "");
    }
}

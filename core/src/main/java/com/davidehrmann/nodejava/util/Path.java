package com.davidehrmann.nodejava.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class Path {
    public static String MODULE_PATH_ELEMENT = "node_modules";

    public static String canonicalizeAbsolutePath(String absolutePath) {
        if (!absolutePath.startsWith("/")) {
            throw new IllegalArgumentException("path '" + absolutePath + "' wasn't absolute");
        }

        // Shortcut for detecting canonical paths
        if ("/".equals(absolutePath) || !absolutePath.endsWith("/.") && !absolutePath.endsWith("/..") &&
                !absolutePath.contains("//") && !absolutePath.contains("./") && !absolutePath.contains("../")) {
            return absolutePath;
        }

        String[] components = absolutePath.split("/");
        Deque<String> canonicalPath = new ArrayDeque<>(components.length);

        for (String component : components) {
            if (".".equals(component) || "".equals(component)) {
                // Do nothing
            } else if ("..".equals(component)) {
                if (!canonicalPath.isEmpty()) {
                    canonicalPath.removeLast();
                } else {
                    // TODO: warn
                }
            } else {
                canonicalPath.addLast(component);
            }
        }

        StringBuilder sb = new StringBuilder(absolutePath.length());
        for (String dir : canonicalPath) {
            sb.append('/').append(dir);
        }

        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return "/";
        }
    }
}

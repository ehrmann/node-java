package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.util.Path;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NodePathResolutionStrategy implements PathResolutionStrategy {

    private static final String[] NO_SUFFIXES = {""};
    private static final String[] SUFFIXES = {".js", "/index.js", "/package.json", ""};

    public Iterator<String> getLookupQueue(String pwd, String path) {
        return new PathIterator(pwd, path);
    }

    private class PathIterator implements Iterator<String> {
        private final String[] suffixes;
        private final boolean search;
        private final String infix;
        private final String path;
        private volatile String pwd;
        private volatile int suffix;

        public PathIterator(String pwd, String path) {
            if (path.endsWith(".js")) {
                suffixes = NO_SUFFIXES;
            } else {
                suffixes = SUFFIXES;
            }

            search = !(path.startsWith("/") || path.startsWith("./") || path.startsWith("../"));

            this.path = path;
            this.pwd = pwd;
            this.suffix = 0;
            this.infix = this.search ? "/" + Path.MODULE_PATH_ELEMENT + "/" : "/";
        }

        @Override
        public boolean hasNext() {
            return (search && !"/".equals(pwd)) || suffix != suffixes.length;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (suffix == suffixes.length) {
                suffix = 0;
                pwd = Path.canonicalizeAbsolutePath(pwd + "/..");
            }

            return Path.canonicalizeAbsolutePath(pwd + infix + path + suffixes[suffix++]);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

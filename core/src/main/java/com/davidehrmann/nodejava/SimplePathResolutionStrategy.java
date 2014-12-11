package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.util.Path;

import java.util.Collections;
import java.util.Iterator;

public final class SimplePathResolutionStrategy implements PathResolutionStrategy {

    public static final PathResolutionStrategy INSTANCE = new SimplePathResolutionStrategy();

    private SimplePathResolutionStrategy() {
    }

    public Iterator<String> getLookupQueue(String pwd, String path) {
        return Collections.singleton(Path.canonicalizeAbsolutePath(pwd + "/" + path)).iterator();
    }
}

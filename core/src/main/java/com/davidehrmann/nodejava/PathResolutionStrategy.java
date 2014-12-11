package com.davidehrmann.nodejava;

import java.util.Iterator;

public interface PathResolutionStrategy {
    Iterator<String> getLookupQueue(String pwd, String path);
}

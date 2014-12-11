package com.davidehrmann.nodejava.scriptloader;

import java.io.IOException;
import java.io.InputStream;

public interface ScriptLoader {
    InputStream loadScript(String canonicalPath) throws IOException;
}

package com.davidehrmann.nodejava.script;

import com.davidehrmann.nodejava.Module;

public interface ScriptRunner<R, S> {
    R exec(S script, Module<R, S> module);
}

package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.script.ScriptRunner;
import com.davidehrmann.nodejava.scriptloader.ResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ResolvingScriptLoader.ResolvedScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// https://nodejs.org/api/modules.html
public class Module<R, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Module.class);

    protected final String pwd;

    protected final ResolvingScriptLoader<S> scriptLoader;
    protected final ScriptRunner<R, S> scriptRunner;

    private final Map<String, R> builtins;
    private final Map<ResolvedScript<S>, R> memos;
    private final ResolvedScript<S> script;
    // Properties exposed in Node
    protected R exports;
    protected R global;
    protected String id;
    protected String filename;
    protected Module main;
    protected Module parent;
    protected List<Module> children = new ArrayList<>();
    private boolean loaded = false;

    public Module(Map<String, R> builtins, String initalPwd, ScriptRunner<R, S> scriptRunner, ResolvingScriptLoader<S> scriptLoader, String filename) {
        this.filename = Objects.requireNonNull(filename, "filename was null");
        this.scriptLoader = Objects.requireNonNull(scriptLoader, "scriptLoader was null");
        this.scriptRunner = Objects.requireNonNull(scriptRunner, "scriptRunner was null");
        this.builtins = Objects.requireNonNull(builtins, "builtins was null");

        this.memos = new IdentityHashMap<>();
        this.pwd = initalPwd;

        this.main = this;
        this.script = null;
    }

    public Module(Map<String, R> builtins, ScriptRunner<R, S> scriptRunner, ResolvingScriptLoader<S> scriptLoader, ResolvedScript<S> script) {
        this.scriptLoader = Objects.requireNonNull(scriptLoader, "scriptLoader was null");
        this.script = Objects.requireNonNull(script, "script was null");
        this.scriptRunner = Objects.requireNonNull(scriptRunner, "scriptRunner was null");
        this.builtins = Objects.requireNonNull(builtins, "builtins was null");
        this.filename = script.getFile();

        this.memos = new IdentityHashMap<>();
        this.pwd = script.getAbsoluteDir();
        this.main = this;
    }

    private Module(Module<R, S> parent, ResolvedScript<S> script, String id) {
        this.parent = Objects.requireNonNull(parent, "parent was null");
        this.pwd = script.getAbsoluteDir();
        this.script = script;
        this.id = id;

        // Copy these fields from the parent so require isn't slow
        this.memos = parent.memos;
        this.main = parent.main;
        this.scriptLoader = parent.scriptLoader;
        this.scriptRunner = parent.scriptRunner;
        this.builtins = parent.builtins;
        this.global = parent.global;
    }

    /**
     * Evaluate the script and return the result
     *
     * @return result of the script
     */
    public R run() {
        R result = null;

        if (this.script != null) {
            try {
                result = this.scriptRunner.exec(this.script.getScript(), this);
                this.loaded = true;
            } finally {
                this.memos.put(script, this.exports);
            }
        }
        return result;
    }

    public R require(String id) {
        LOGGER.debug("require('{}') from {}", id, this.id);

        // If this is a built-in, serve it
        if (this.builtins.containsKey(id)) {
            return builtins.get(id);
        }

        // Otherwise, look it up from the script loader
        // TODO: does looking for node_modules belong in here?
        final ResolvedScript<S> script = this.scriptLoader.loadScript(id, this.pwd);
        if (script == null) {
            LOGGER.warn("require('{}') from {} failed", id, this.id);
            // TODO: If the given path does not exist, require() will throw an Error with its code property set to 'MODULE_NOT_FOUND'.
            // TODO: throw exception from factory
            throw new RuntimeException(id + " not found");
        }

        // Serve this out of the cache if possible
        if (this.memos.containsKey(script)) {
            return this.memos.get(script);
        }

        Module<R, S> child = new Module<>(this, script, script.getAbsoluteDir() + script.getFile());
        this.children.add(child);
        child.run();
        return child.getExports();
    }

    public R getGlobal() {
        return this.global;
    }

    public void setGlobal(R global) {
        this.global = global;
    }

    public R getExports() {
        return this.exports;
    }

    public void setExports(R exports) {
        if (this.loaded) {
            LOGGER.warn("exports in {} modified after module creation", this.id);
        }

        // TODO: also warn on other module modifying it

        this.exports = exports;
        this.memos.put(script, this.exports);
    }

    public Map<String, R> getBuiltins() {
        return this.builtins;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public Module getMain() {
        return main;
    }

    public Module getParent() {
        return parent;
    }

    public List<Module> getChildren() {
        return children;
    }
}

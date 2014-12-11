package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.Module;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RhinoModuleAdapter extends AbstractRuntimeObject {
    private static final long serialVersionUID = -8413606781796676262L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RhinoModuleAdapter.class);

    protected final Module<Object, Script> module;

    public RhinoModuleAdapter(final Module<Object, Script> module, Context cx, Scriptable scope) {
        super(cx, scope, "module? module.id?");
        this.module = Objects.requireNonNull(module, "module was null");

        // Adapt require
        super.defaultPut("require", new AbstractRuntimeFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                if (args.length >= 1 && args[0] != null) {
                    String id = args[0].toString();
                    return module.require(id);
                }

                // TODO: how to fail?
                return Context.getUndefinedValue();
            }
        });

        super.defaultPut("exports", cx.newObject(scope));
        super.defaultPut("main", module.getMain());
        super.defaultPut("id", module.getId());
        super.defaultPut("filename", module.getFilename());

        // TODO: this one is tricky to get right because of scopes
        // super.defaultPut("parent", new RhinoModuleAdapter(module.parent, module.parent., scope));

        // TODO: adapt  module.children to NativeArray
        super.defaultPut("children", null);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        super.put(name, start, value);
        if ("exports".equals(name)) {
            this.module.setExports(value);
        }
    }

    @Override
    public void putConst(String name, Scriptable start, Object value) {
        super.putConst(name, start, value);
        if ("exports".equals(name)) {
            this.module.setExports(value);
        }
    }

    @Override
    public void delete(String name) {
        super.delete(name);
        if ("exports".equals(name)) {
            LOGGER.warn("exports from {} deleted", this.module.getId());
            this.module.setExports(Context.getUndefinedValue());
        }
    }
}

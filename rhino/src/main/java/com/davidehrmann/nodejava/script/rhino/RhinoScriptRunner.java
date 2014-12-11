package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.Module;
import com.davidehrmann.nodejava.script.ScriptRunner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RhinoScriptRunner implements ScriptRunner<Object, Script> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RhinoScriptRunner.class);

    @Override
    public Object exec(Script script, Module<Object, Script> module) {
        Object result = Context.getUndefinedValue();
        Context cx = ContextFactory.getGlobal().enterContext();
        try {
            Scriptable moduleScope = cx.initStandardObjects();

            if (module.getGlobal() == null) {
                module.setGlobal(cx.newObject(moduleScope));
            }

            Object exports = cx.newObject(moduleScope);
            module.setExports(exports);

            RhinoModuleAdapter adaptedModule = new RhinoModuleAdapter(module, cx, moduleScope);

            moduleScope.put("global", moduleScope, module.getGlobal());
            moduleScope.put("module", moduleScope, adaptedModule);
            moduleScope.put("exports", moduleScope, exports);
            moduleScope.put("require", moduleScope, adaptedModule.get("require"));
            moduleScope.put("console", moduleScope, module.getBuiltins().get("console"));

            result = script.exec(cx, moduleScope);
        } finally {
            Context.exit();
        }
        // TODO: catch something before the finally
        return result;
    }
}

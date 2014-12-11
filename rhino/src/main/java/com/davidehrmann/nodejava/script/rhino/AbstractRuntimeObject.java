package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

@Deprecated
class AbstractRuntimeObject extends NativeObject {

    private static final long serialVersionUID = 2427223494177205960L;

    protected final Context cx;
    protected final Scriptable scope;
    protected final String defaultValue;

    public AbstractRuntimeObject(Context cx, Scriptable scope, String defaultValue) {
        if (cx == null) {
            throw new NullPointerException("context was null");
        }
        if (scope == null) {
            throw new NullPointerException("scope was null");
        }

        this.cx = cx;
        this.scope = scope;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return this.defaultValue;
    }

    public abstract class AbstractRuntimeFunction extends BaseFunction {
        private static final long serialVersionUID = 2936279843075497840L;

        public AbstractRuntimeFunction() {
            super(AbstractRuntimeObject.this.scope, AbstractRuntimeObject.this.scope);
        }

        @Override
        public abstract Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args);
    }
}

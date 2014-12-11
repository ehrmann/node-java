package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

public class FloatArrayAdapter extends AbstractArrayAdapter<float[]> {

    FloatArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, float[] adapted) {
        super(adapterFactory, parentScope, adapted);
    }

    @Override
    protected Object coerce(Object value) {
        float coerced;
        if (value instanceof Boolean) {
            coerced = (Boolean) value ? 1 : 0;
        } else if (value instanceof Number) {
            coerced = ((Number) value).floatValue();
        } else {
            coerced = value != null ? 1 : 0;
        }

        return coerced;
    }
}

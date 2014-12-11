package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

public class ShortArrayAdapter extends AbstractArrayAdapter<short[]> {

    ShortArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, short[] adapted) {
        super(adapterFactory, parentScope, adapted);
    }

    @Override
    protected Object coerce(Object value) throws IllegalArgumentException {
        short coerced;
        if (value instanceof Boolean) {
            coerced = (Boolean) value ? (short) 1 : 0;
        } else if (value instanceof Number) {
            coerced = ((Number) value).shortValue();
        } else {
            coerced = value != null ? (short) 1 : 0;
        }

        return coerced;
    }
}

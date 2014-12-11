package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

public class IntArrayAdapter extends AbstractArrayAdapter<int[]> {

    IntArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, int[] adapted) {
        super(adapterFactory, parentScope, adapted);
    }

    @Override
    protected Object coerce(Object value) throws IllegalArgumentException {
        int coerced;
        if (value instanceof Boolean) {
            coerced = (Boolean) value ? 1 : 0;
        } else if (value instanceof Number) {
            coerced = ((Number) value).intValue();
        } else {
            coerced = value != null ? 1 : 0;
        }

        return coerced;
    }
}

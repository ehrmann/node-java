package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

public class BooleanArrayAdapter extends AbstractArrayAdapter<boolean[]> {

    BooleanArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, boolean[] adapted) {
        super(adapterFactory, parentScope, adapted);
    }

    @Override
    protected Object coerce(Object value) throws IllegalArgumentException {
        boolean coerced;
        if (value instanceof Boolean) {
            coerced = (Boolean) value;
        } else if (value instanceof Number) {
            coerced = ((Number) value).intValue() != 0;
        } else {
            coerced = value != null;
        }

        return coerced;
    }
}

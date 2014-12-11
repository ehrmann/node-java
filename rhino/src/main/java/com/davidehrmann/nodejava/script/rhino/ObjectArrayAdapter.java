package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

public class ObjectArrayAdapter extends AbstractArrayAdapter<Object[]> {

    ObjectArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, Object[] adapted) {
        super(adapterFactory, parentScope, adapted);
    }

    @Override
    protected Object coerce(Object value) throws IllegalArgumentException {
        return value;
    }
}

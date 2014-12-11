package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;

import java.util.AbstractList;
import java.util.Objects;

class RhinoArrayAdapter extends AbstractList<Object> implements AdaptedRhino {

    private RhinoAdapterFactory adapterFactory;
    private final Scriptable scriptable;

    public RhinoArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable scriptable) {
        this.adapterFactory = adapterFactory;
        this.scriptable = Objects.requireNonNull(scriptable);
    }

    @Override
    public Object get(int index) {
        return adapterFactory.toJava(scriptable.get(index, null));
    }

    @Override
    public int size() {
        return scriptable.getIds().length;
    }

    @Override
    public Object getAdapted() {
        return scriptable;
    }
}

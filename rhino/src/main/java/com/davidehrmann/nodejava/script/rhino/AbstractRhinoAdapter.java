package com.davidehrmann.nodejava.script.rhino;

import java.util.Objects;

public class AbstractRhinoAdapter<T> implements AdaptedRhino<T> {

    private final RhinoAdapterFactory adapterFactory;
    private final T adapted;

    public AbstractRhinoAdapter(RhinoAdapterFactory adapterFactory, T adapted) {
        this.adapterFactory = Objects.requireNonNull(adapterFactory);
        this.adapted = adapted;
    }

    @Override
    public T getAdapted() {
        return adapted;
    }

    public RhinoAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }
}

package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

class FunctionAdapter extends BaseFunction implements Adapted<Function<Object[], Object>> {
    private RhinoAdapterFactory adapterFactory;
    private final Function<Object[], Object> adapted;

    public FunctionAdapter(RhinoAdapterFactory adapterFactory, Function<Object[], Object> adapted) {
        this.adapterFactory = Objects.requireNonNull(adapterFactory);
        this.adapted = Objects.requireNonNull(adapted);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, final Object[] args) {
        Object[] javaArgs = Arrays.stream(args).map(adapterFactory::toJava).toArray();
        return adapterFactory.fromJava(adapted.apply(javaArgs));
    }

    @Override
    public Function<Object[], Object> getAdapted() {
        return adapted;
    }
}

package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Context;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class RhinoFunctionAdapter extends AbstractRhinoAdapter<org.mozilla.javascript.Function> implements Function<Object[], Object> {

    public RhinoFunctionAdapter(RhinoAdapterFactory adapterFactory, org.mozilla.javascript.Function adapted) {
        super(adapterFactory, Objects.requireNonNull(adapted));
    }

    @Override
    public Object apply(Object[] objects) {
        Object[] jsParams = Stream.of(objects).map(getAdapterFactory()::fromJava).toArray();

        Context context = Context.enter();
        try {
            return getAdapterFactory().toJava(
                    getAdapted().call(context, context.initStandardObjects(), null, jsParams)
            );
        } finally {
            Context.exit();
        }
    }
}

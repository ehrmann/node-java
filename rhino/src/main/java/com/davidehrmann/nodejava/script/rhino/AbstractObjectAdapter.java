package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Objects;

public abstract class AbstractObjectAdapter<T> extends ScriptableObject implements Adapted<T> {

    private final RhinoAdapterFactory adapterFactory;
    private final T adapted;
    private volatile Scriptable prototype;

    AbstractObjectAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, T adapted) {
        this.adapterFactory = Objects.requireNonNull(adapterFactory);
        this.adapted = Objects.requireNonNull(adapted);

        setParentScope(Objects.requireNonNull(parentScope));
        this.prototype = TopLevel.getBuiltinPrototype(parentScope, TopLevel.Builtins.Object);
    }

    @Override
    public T getAdapted() {
        return adapted;
    }

    @Override
    public String getClassName() {
        return "Object";
    }

    @Override
    public Scriptable getPrototype() {
        return this.prototype;
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return Scriptable.class;
    }

    protected RhinoAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new NotSerializableException();
    }
}

package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.ExternalArrayData;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.TopLevel;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

abstract class AbstractArrayAdapter<T> extends NativeArray implements Adapted<T> {

    private static final Method SET_DENSE_ONLY;
    static {
        try {
            SET_DENSE_ONLY = NativeArray.class.getDeclaredMethod("setDenseOnly", boolean.class);
            SET_DENSE_ONLY.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("NativeArray.setDenseOnly(boolean) not found. Incompatible Rhino version?", e);
        }
    }

    private final T adapted;
    private final int length;
    private final RhinoAdapterFactory adapterFactory;

    AbstractArrayAdapter(RhinoAdapterFactory adapterFactory, Scriptable scope, T array) {
        super(new Object[0]);

        this.length = Array.getLength(array);
        this.adapterFactory = Objects.requireNonNull(adapterFactory);
        this.adapted = array;

        ScriptRuntime.setBuiltinProtoAndParent(this, scope, TopLevel.Builtins.Array);
        super.setExternalArrayData(new AdaptedExternalArrayData());

        try {
            SET_DENSE_ONLY.invoke(this, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getAdapted() {
        return adapted;
    }

    @Override
    public void delete(int index) {
        if (index >= 0 && index < length) {
            Array.set(adapted, index, coerce(null));
        }
    }

    @Override
    public long getLength() {
        return length;
    }

    protected abstract Object coerce(Object value) throws IllegalArgumentException;

    /*
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

    */
/*
    @Override
    public boolean has(int index, Scriptable start) {
        return index >= 0 && index < length;
    }

    @Override
    public Object get(String name, Scriptable start) {
        if ("length".equals(name)) {
            return (double) length;
        } else {
            return super.get(name, start);
        }
    }

    @Override
    public Object get(int index, Scriptable start) {
        if (index < 0 || index >= length) {
            return Scriptable.NOT_FOUND;
        } else {
            return getAdapterFactory().fromJava(Array.get(getAdapted(), index));
        }
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
    }

    @Override
    public void delete(String name) {
    }

    @Override
    public Object[] getIds() {
        Object[] ids;
        WeakReference<Object[]> idsReference = this.idsReference;

        if (idsReference == null || (ids = idsReference.get()) == null) {
            ids = IntStream.range(0, length).mapToObj(Integer::valueOf).toArray();
            this.idsReference = new WeakReference<>(ids);
        }

        return ids;
    }

    @Override
    public Object[] getAllIds() {
        Object[] allIds;
        WeakReference<Object[]> allIdsReference = this.allIdsReference;

        if (allIdsReference == null || (allIds = allIdsReference.get()) == null) {
            allIds = Stream.concat(
                    Arrays.stream(super.getAllIds()),
                    IntStream.range(0, length).mapToObj(Integer::valueOf)
            ).toArray();
            this.allIdsReference = new WeakReference<>(allIds);
        }

        return allIds;
    }

    @Override
    public void delete(int index) {
        super.delete(index);
    }

    @Override
    public long getLength() {
        return length;
    }

    protected RhinoAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }
*/
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new NotSerializableException();
    }

    private class AdaptedExternalArrayData implements ExternalArrayData {

        @Override
        public Object getArrayElement(int index) {
            if (index < 0 || index >= length) {
                return Scriptable.NOT_FOUND;
            } else {
                return adapterFactory.fromJava(Array.get(adapted, index));
            }
        }

        @Override
        public void setArrayElement(int index, Object value) {
            if (index >= 0 && index < length) {
                Object coerced = coerce(adapterFactory.toJava(value));
                Array.set(adapted, index, coerced);
            }
        }

        @Override
        public int getArrayLength() {
            return length;
        }
    }
}

package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ExternalArrayData;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

public class ListAdapter<T> extends NativeArray implements Adapted<List<T>> {

    private static final Method SET_DENSE_ONLY;
    static {
        try {
            SET_DENSE_ONLY = NativeArray.class.getDeclaredMethod("setDenseOnly", boolean.class);
            SET_DENSE_ONLY.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("NativeArray.setDenseOnly(boolean) not found. Incompatible Rhino version?", e);
        }
    }

    private final List<T> adapted;
    private final RhinoAdapterFactory adapterFactory;
    private Set<Integer> undefined = new HashSet<>();

    ListAdapter(RhinoAdapterFactory adapterFactory, Scriptable scope, List<T> adapted) {
        super(new Object[0]);

        this.adapterFactory = Objects.requireNonNull(adapterFactory);
        this.adapted = Objects.requireNonNull(adapted);

        ScriptRuntime.setBuiltinProtoAndParent(this, scope, TopLevel.Builtins.Array);
        super.setExternalArrayData(new AdaptedExternalArrayData());

        try {
            SET_DENSE_ONLY.invoke(this, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> getAdapted() {
        return adapted;
    }

    @Override
    public void delete(int index) {
        undefined.add(index);

        if (index + 1 == adapted.size()) {
            ListIterator<T> i = adapted.listIterator(adapted.size());
            while (i.hasPrevious() && undefined.remove(i.previousIndex())) {
                i.previous();
                i.remove();
            }
        } else if (index >= 0 && index < adapted.size()) {
            adapted.set(index, null);
        }
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        if (start != this) {
            super.put(index, start, value);
        } else if (!isSealed()) {
            super.getExternalArrayData().setArrayElement(index, value);
        }
    }

    @Override
    public long getLength() {
        return adapted.size();
    }

    @Override
    public Object[] getIds() {
        return IntStream.range(0, adapted.size()).mapToObj(Integer::valueOf).toArray();
    }

    @Override
    protected ScriptableObject getOwnPropertyDescriptor(Context cx, Object id) {
        if (id instanceof Integer) {
            int index = (Integer) id;
            if (index >= 0 && index < adapted.size()) {
                return ScriptableObject.buildDataDescriptor(
                        getParentScope(),
                        adapterFactory.fromJava(adapted.get(index)),
                        isSealed() ? (ScriptableObject.READONLY | ScriptableObject.CONST) : 0
                );
            } else {
                return null;
            }
        } else {
            return super.getOwnPropertyDescriptor(cx, id);
        }
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

    private class AdaptedExternalArrayData implements ExternalArrayData {

        @Override
        public Object getArrayElement(int index) {
            try {
                Object result = adapted.get(index);
                Object adaptedResult = adapterFactory.fromJava(result);

                if (undefined.contains(index)) {
                    if (result != null) {
                        undefined.remove(index);
                    } else {
                        adaptedResult = Scriptable.NOT_FOUND;
                    }
                }

                return adaptedResult;
            } catch (IndexOutOfBoundsException e) {
                undefined.remove(index);
                return Scriptable.NOT_FOUND;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setArrayElement(int index, Object value) {
            if (index >= 0) {
                if (index < getAdapted().size()) {
                    getAdapted().set(index, (T) adapterFactory.toJava(value));
                    undefined.remove(index);
                } else {
                    ListIterator<T> i = adapted.listIterator(adapted.size());
                    while (i.nextIndex() < index) {
                        i.add(null);
                        undefined.add(index);
                    }
                    i.add((T) adapterFactory.toJava(value));
                }
            }
        }

        @Override
        public int getArrayLength() {
            return adapted.size();
        }
    }
}

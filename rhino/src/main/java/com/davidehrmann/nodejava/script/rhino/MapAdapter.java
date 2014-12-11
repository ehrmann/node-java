package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.davidehrmann.nodejava.script.AdapterFactory.JAVA_UNDEFINED;

class MapAdapter extends AbstractObjectAdapter<Map<Object, Object>> {

    private static final Object KEY_NOT_PRESENT = new Object();

    MapAdapter(RhinoAdapterFactory adapterFactory, Scriptable parentScope, Map<Object, Object> adapted) {
        super(adapterFactory, parentScope, adapted);

        // TODO: Heuristic for detecting if object is immutable, then seal the adapted object
        // if this is a Guava ImmutableMap, annotated with net.jcip.annotations.Immutable,
        // java.util.Collections.UnmodifiableMap, java.util.Collections.SingletonMap,
        // java.util.Collections.EmptyMap, or org.apache.commons.collections4.Unmodifiable
        // scala.collection.immutable.Map, clojure.lang.IPersistentMap
    }

    @Override
    public Object get(String name, Scriptable start) {
        Object javaResult = getAdapted().getOrDefault(name, JAVA_UNDEFINED);
        if (javaResult != JAVA_UNDEFINED) {
            return getAdapterFactory().fromJava(javaResult);
        } else {
            return Scriptable.NOT_FOUND;
        }
    }

    @Override
    public Object get(int index, Scriptable start) {
        Object javaResult = getAdapted().getOrDefault((double) index, JAVA_UNDEFINED);
        if (javaResult != JAVA_UNDEFINED) {
            return getAdapterFactory().fromJava(javaResult);
        } else {
            return Scriptable.NOT_FOUND;
        }
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return getAdapted().containsKey(name);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return getAdapted().containsKey((double) index);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        if (isExtensible()) {
            getAdapted().put(name, getAdapterFactory().toJava(value));
        } else {
            getAdapted().replace(name, getAdapterFactory().toJava(value));
        }
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        if (isExtensible()) {
            getAdapted().put((double) index, getAdapterFactory().toJava(value));
        } else {
            getAdapted().replace((double) index, getAdapterFactory().toJava(value));
        }
    }

    @Override
    public void delete(String name) {
        if (isExtensible()) {
            getAdapted().remove(name);
        }
    }

    @Override
    public void delete(int index) {
        if (isExtensible()) {
            getAdapted().remove((double) index);
        }
    }

    @Override
    public Object[] getIds() {
        // This only exposes entries with JS-compatible keys
        return getAdapted().keySet().stream()
                .filter(key -> key instanceof CharSequence || key instanceof Integer)
                .map(getAdapterFactory()::fromJava)
                .toArray();
    }

    @Override
    public Object[] getAllIds() {
        return Stream.concat(
                Arrays.stream(super.getAllIds()),
                getAdapted().keySet().stream()
                        .filter(key -> key instanceof CharSequence || key instanceof Integer)
                        .map(getAdapterFactory()::fromJava)
        ).toArray();
    }

    /*
    @Override
    public boolean hasInstance(Scriptable instance) {
        // Return true if we want instanceof to make this look like a JS Object
        return false;
    }
    */

    @Override
    protected ScriptableObject getOwnPropertyDescriptor(Context cx, Object id) {
        Object key = getAdapterFactory().toJava(id);
        Object value = getAdapted().getOrDefault(key, KEY_NOT_PRESENT);

        if (value != KEY_NOT_PRESENT) {
            return ScriptableObject.buildDataDescriptor(getParentScope(), getAdapterFactory().fromJava(value), 0);
        } else {
            return super.getOwnPropertyDescriptor(cx, id);
        }
    }
}

package com.davidehrmann.nodejava.script.rhino;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.UniqueTag;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class RhinoObjectAdapter extends AbstractMap<Object, Object> implements AdaptedRhino {

    private RhinoAdapterFactory rhinoObjectAdapterFactory;
    private final Scriptable scriptable;

    public RhinoObjectAdapter(RhinoAdapterFactory rhinoObjectAdapterFactory, Scriptable scriptable) {
        this.rhinoObjectAdapterFactory = rhinoObjectAdapterFactory;
        this.scriptable = Objects.requireNonNull(scriptable);
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return new AbstractSet<Entry<Object, Object>>() {
            @Override
            public Iterator<Entry<Object, Object>> iterator() {
                final Object[] ids = scriptable.getIds();
                return new Iterator<Entry<Object, Object>>() {
                    private final AtomicInteger nextIndex = new AtomicInteger();

                    @Override
                    public boolean hasNext() {
                        return nextIndex.get() < ids.length;
                    }

                    @Override
                    public Entry<Object, Object> next() {
                        while (true) {
                            int currentIndex = nextIndex.get();
                            if (currentIndex >= ids.length) {
                                throw new NoSuchElementException();
                            } else if (nextIndex.compareAndSet(currentIndex, currentIndex + 1)) {
                                Object id = ids[currentIndex];
                                Object value;
                                if (id instanceof String || id == null) {
                                    value = scriptable.get((String) id, null);
                                } else {
                                    value = scriptable.get(((Number) id).intValue(), null);
                                }

                                return new SimpleEntry<>(id, rhinoObjectAdapterFactory.toJava(value));
                            }
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return scriptable.getIds().length;
            }

            @Override
            public boolean contains(Object o) {
                if (o instanceof Map.Entry) {
                    Entry<?, ?> entry = (Entry<?, ?>) o;
                    if (entry.getKey() instanceof String || entry.getKey() == null) {
                        Scriptable value = (Scriptable) scriptable.get((String) entry.getKey(), null);
                        return Objects.equals(entry.getValue(), rhinoObjectAdapterFactory.toJava(value));
                    } else if (entry.getValue() instanceof Number) {
                        Scriptable value = (Scriptable) scriptable.get(((Number) entry.getKey()).intValue(), null);
                        return Objects.equals(entry.getValue(), rhinoObjectAdapterFactory.toJava(value));
                    }
                }

                return false;
            }
        };
    }

    @Override
    public int size() {
        return scriptable.getIds().length;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String || key == null) {
            return scriptable.has((String) key, null);
        } else if (key instanceof Number) {
            return scriptable.has(((Number) key).intValue(), null);
        } else {
            return false;
        }
    }

    @Override
    public Object get(Object key) {
        Object result;
        if (key instanceof String || key == null) {
            result = scriptable.get((String) key, null);
        } else if (key instanceof Number) {
            result = scriptable.get(((Number) key).intValue(), null);
        } else {
            return null;
        }

        if (UniqueTag.NOT_FOUND.equals(result)) {
            return null;
        } else {
            return rhinoObjectAdapterFactory.toJava(result);
        }
    }

    @Override
    public Object getAdapted() {
        return scriptable;
    }
}

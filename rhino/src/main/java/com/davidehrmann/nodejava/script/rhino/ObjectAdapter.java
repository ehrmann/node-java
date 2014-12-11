package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.runtime.NodeFunction;
import com.davidehrmann.nodejava.runtime.NodeProperty;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ObjectAdapter extends AbstractObjectAdapter<Object> {

    private final String name = null;

    private final Map<String, Method> getterMethods;
    private final Map<String, org.mozilla.javascript.Function> decoratedMethods = new HashMap<>();
    private final Map<String, Field> fields = new HashMap<>();

    private final Object[] ids;

    public ObjectAdapter(RhinoAdapterFactory adapterFactory, Scriptable prototype, Object adapted) {
        super(adapterFactory, prototype, adapted);

        Map<String, List<Method>> decoratedMethods = new HashMap<>();
        Map<String, Method> getterMethods = new HashMap<>();

        for (Method method : adapted.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (method.getAnnotation(NodeFunction.class) != null) {
                    if (!decoratedMethods.containsKey(method.getName())) {
                        decoratedMethods.put(method.getName(), new ArrayList<>(4));
                    }
                    decoratedMethods.get(method.getName()).add(method);
                }

                NodeProperty nodeProperty = method.getAnnotation(NodeProperty.class);
                if (nodeProperty != null && method.getParameterCount() == 0) {
                    String name = nodeProperty.value().isEmpty() ? method.getName() : nodeProperty.value();
                    getterMethods.put(name, method);
                }
            }
        }

        for (Field field : adapted.getClass().getFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                if (field.getAnnotation(NodeFunction.class) != null || field.getAnnotation(NodeProperty.class) != null) {
                    fields.put(field.getName(), field);
                }
            }
        }

        if (!Collections.disjoint(getterMethods.keySet(), decoratedMethods.keySet())) {
            throw new IllegalArgumentException("Class " + adapted.getClass() + " has NodeProperties and NodeFunctions with the same names");
        }

        for (Map.Entry<String, List<Method>> entry : decoratedMethods.entrySet()) {
            this.decoratedMethods.put(entry.getKey(), new MethodAdapter<>(adapterFactory, adapted, entry.getValue()));
        }

        this.getterMethods = getterMethods;
        this.ids = Stream.concat(this.decoratedMethods.keySet().stream(), this.getterMethods.keySet().stream()).toArray();
    }

    @Override
    public String getClassName() {
        return name;
    }

    @Override
    public Object get(String name, Scriptable start) {
        Method getter;
        org.mozilla.javascript.Function function;
        Field field;

        if ((getter = getterMethods.get(name)) != null) {
            try {
                return getter.invoke(getAdapted());
            } catch (IllegalAccessException e) {
                throw new RuntimeException();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
                // TODO: rethrow
            }
        } else if ((function = decoratedMethods.get(name)) != null) {
            return function;
        } else if ((field = fields.get(name)) != null) {
            try {
                return getAdapterFactory().fromJava(field.get(getAdapted()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Undefined.instance;
        }
    }

    @Override
    public Object get(int index, Scriptable start) {
        return Undefined.instance;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return getterMethods.containsKey(name) || decoratedMethods.containsKey(name);
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return false;
    }

    @Override
    public void put(String name, Scriptable start, Object value) {

    }

    @Override
    public void put(int index, Scriptable start, Object value) {

    }

    @Override
    public void delete(String name) {

    }

    @Override
    public void delete(int index) {

    }

    @Override
    public Object[] getIds() {
        return ids;
    }

}

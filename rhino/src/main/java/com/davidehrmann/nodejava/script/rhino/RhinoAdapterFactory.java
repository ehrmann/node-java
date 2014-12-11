package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.script.AdapterFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RhinoAdapterFactory implements AdapterFactory<Object> {

    protected final Context context;
    protected final Scriptable scope;

    public RhinoAdapterFactory(Context context) {
        this.context = Objects.requireNonNull(context);
        this.scope = context.initStandardObjects();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object fromJava(Object root) {
        Object result;

        if (root == null) {
            result = ScriptRuntime.toObjectOrNull(context, null, scope);
        } else if (root == JAVA_UNDEFINED) {
            // result = Context.getUndefinedValue();
            result = Undefined.instance;
        } else if (root instanceof AdaptedRhino) {
            return ((AdaptedRhino) root).getAdapted();
        } else if (root instanceof Map) {
            result = new MapAdapter(this, scope, (Map<Object, Object>) root);
        } else if (root instanceof List) {
            return new ListAdapter(this, scope, (List<Object>) root);
        } else if (root instanceof Collection) {
            // TODO: use an adapter (but these will be messy)
            Scriptable array = context.newArray(scope, ((Collection) root).size());
            result = array;
            int index = 0;
            for (Object o : (Collection) root) {
                array.put(index++, array, fromJava(o));
            }
        } else if (root instanceof boolean[]) {
            result = new BooleanArrayAdapter(this, scope, (boolean[]) root);
        } else if (root instanceof short[]) {
            result = new ShortArrayAdapter(this, scope, (short[]) root);
        } else if (root instanceof int[]) {
            result = new IntArrayAdapter(this, scope, (int[]) root);
        } else if (root instanceof long[]) {
            result = new LongArrayAdapter(this, scope, (long[]) root);
        } else if (root instanceof float[]) {
            result = new FloatArrayAdapter(this, scope, (float[]) root);
        } else if (root instanceof double[]) {
            result = new DoubleArrayAdapter(this, scope, (double[]) root);
        } else if (root instanceof Object[]) {
            result = new ObjectArrayAdapter(this, scope, (Object[]) root);

            // TODO: more adapters
        /*
                || )
                || char[].class.isAssignableFrom(root.getClass())
                ||
                || long[].class.isAssignableFrom(root.getClass())
                || float[].class.isAssignableFrom(root.getClass())
                || double[].class.isAssignableFrom(root.getClass())
                || Object[].class.isAssignableFrom(root.getClass())
                ) {

            Object[] f = new String[10];

            Scriptable array = context.newArray(scope, Array.getLength(root));
            result = array;
            for (int i = 0; i < Array.getLength(root); i++) {
                array.put(i, array, fromJava(root));
            }
            */
        } else if (root instanceof CharSequence || root instanceof Number || root instanceof Boolean) {
            result = ScriptRuntime.toObject(context, scope, root);
        } else if (root instanceof char[]) {
            result = new CharArrayAdapter(context, scope, (char[]) root);
        } else if (root instanceof java.util.function.Function) {
            result = new FunctionAdapter(this, (java.util.function.Function<Object[], Object>) root);
        } else {
            /*
            Scriptable object = context.newObject(scope);

            Map<String, List<Method>> decoratedMethods = new HashMap<>();
            Map<String, Method> getterMethods = new HashMap<>();

            for (Method method : root.getClass().getMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    if (method.getAnnotation(NodeFunction.class) != null) {
                        if (!decoratedMethods.containsKey(method.getName())) {
                            decoratedMethods.put(method.getName(), new ArrayList<>(4));
                        }
                        decoratedMethods.get(method.getName()).add(method);
                    }

                    NodeProperty nodeProperty =  method.getAnnotation(NodeProperty.class);
                    if (nodeProperty != null && method.getParameterCount() == 0) {
                        String name = nodeProperty.value().isEmpty() ? method.getName() : nodeProperty.value();
                        getterMethods.put(name, method);
                    }
                }
            }

            for (Map.Entry<String, List<Method>> entry : decoratedMethods.entrySet()) {
                object.put(entry.getKey(), object, new MethodAdapter<>(this, root, entry.getValue()));
            }
            result = object;
            */
            result = new ObjectAdapter(this, scope, root);
        }

        return result;
    }

    @Override
    public Object toJava(Object object) {
        if (object == null) {
            return null;
        } else if (Context.getUndefinedValue().equals(object)) {
            return JAVA_UNDEFINED;
        } else if (object instanceof CharSequence) {
            return object.toString();
        } else if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof Boolean) {
            return object;
        } else if (object instanceof Adapted) {
            return ((Adapted) object).getAdapted();
        }

        Scriptable scriptable = (Scriptable) object;

        if (hasPrototype(scriptable, "String")) {
            return scriptable.toString();
        } else if (hasPrototype(scriptable, "Number")) {
            return ScriptRuntime.toNumber(scriptable);
        } else if (hasPrototype(scriptable, "Boolean")) {
            return ScriptRuntime.toBoolean(scriptable);
        } else if (scriptable instanceof Function) {
            return new RhinoFunctionAdapter(this, (Function) scriptable);
        } else if (hasPrototype(scriptable, "Array")) {
            return new RhinoArrayAdapter(this, scriptable);
        } else {
            return new RhinoObjectAdapter(this, scriptable);
        }
    }

    protected boolean hasPrototype(Scriptable scriptable, String className) {
        while (scriptable != null) {
            if (className.equals(scriptable.getClassName())) {
                return true;
            }
            scriptable = scriptable.getPrototype();
        }

        return false;
    }

}

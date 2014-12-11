package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.script.AdapterFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class MethodAdapter<T> extends org.mozilla.javascript.BaseFunction {

    private static final Set<Class<? extends Number>> NUMBER_PRIMITIVE_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            int.class, short.class, double.class, float.class, long.class
    )));

    private RhinoAdapterFactory adapterFactory;
    private final Collection<Method> methods;
    private final T thiz;

    public MethodAdapter(RhinoAdapterFactory adapterFactory, T thiz, Collection<Method> methods) {
        this.adapterFactory = adapterFactory;
        this.methods = Objects.requireNonNull(methods);
        this.thiz = thiz;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        Object[] javaArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            javaArgs[i] = adapterFactory.toJava(args[i]);
        }

        Method bestMatch = null;
        methodsLoop:
        for (Method method : methods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length <= javaArgs.length) {
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (javaArgs[i] instanceof Number && NUMBER_PRIMITIVE_CLASSES.contains(parameterTypes[i])) {

                    } else if (javaArgs[i] instanceof Boolean && parameterTypes[i] == boolean.class) {

                    } else if (javaArgs[i] != null && !parameterTypes[i].isInstance(javaArgs[i])) {
                        continue methodsLoop;
                    } else if (javaArgs[i] == null && parameterTypes[i].isPrimitive()) {
                        continue methodsLoop;
                    }
                }

                if (bestMatch == null || bestMatch.getParameterTypes().length < method.getParameterTypes().length) {
                    bestMatch = method;
                }
            }
        }

        if (bestMatch != null) {
            Object[] fixedArgs = new Object[bestMatch.getParameterTypes().length];
            for (int i = 0; i < fixedArgs.length && i < javaArgs.length; i++) {
                if (javaArgs[i] instanceof Number) {
                    if (bestMatch.getParameterTypes()[i] == int.class) {
                        fixedArgs[i] = ((Number) javaArgs[i]).intValue();
                    } else if (bestMatch.getParameterTypes()[i] == long.class) {
                        fixedArgs[i] = ((Number) javaArgs[i]).longValue();
                    } else if (bestMatch.getParameterTypes()[i] == short.class) {
                        fixedArgs[i] = ((Number) javaArgs[i]).shortValue();
                    } else if (bestMatch.getParameterTypes()[i] == double.class) {
                        fixedArgs[i] = ((Number) javaArgs[i]).doubleValue();
                    } else if (bestMatch.getParameterTypes()[i] == float.class) {
                        fixedArgs[i] = ((Number) javaArgs[i]).floatValue();
                    }

                    // TODO: warn about precision loss
                } else {
                    fixedArgs[i] = javaArgs[i];
                }
            }

            try {
                Object result = bestMatch.invoke(thiz, fixedArgs);
                if (void.class.equals(bestMatch.getReturnType())) {
                    result = AdapterFactory.JAVA_UNDEFINED;
                }
                return adapterFactory.fromJava(result);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(adapterFactory.getClass().getSimpleName() + " didn't validate permissions correctly?", e);
            } catch (InvocationTargetException e) {
                // TODO: bubble up?
            }
        }

        // TODO: update exception type?
        throw new EvaluatorException("Function not found");
    }
}

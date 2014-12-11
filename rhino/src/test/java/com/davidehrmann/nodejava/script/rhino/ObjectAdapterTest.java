package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.runtime.NodeFunction;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ObjectAdapterTest {

    private Context cx = Context.enter();
    private RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);
    private Scriptable scope = cx.initSafeStandardObjects();

    @Test
    public void testOverloadedMethods() {
        scope.put("o", scope, adapterFactory.fromJava(new AnnotatedClass()));

        assertEquals("0", cx.evaluateString(scope, "o.overloadedFunction()", "test.js", 1, null).toString());
        assertEquals("1", cx.evaluateString(scope, "o.overloadedFunction('')", "test.js", 1, null).toString());
        assertEquals("2", cx.evaluateString(scope, "o.overloadedFunction('', '')", "test.js", 1, null).toString());
        assertEquals("3", cx.evaluateString(scope, "o.overloadedFunction('', '', '')", "test.js", 1, null).toString());

        assertEquals("42", cx.evaluateString(scope, "o.intFunction(42)", "test.js", 1, null).toString());
        assertEquals("6.125", cx.evaluateString(scope, "o.floatFunction(6.125)", "test.js", 1, null).toString());

        assertSame(Context.getUndefinedValue(), cx.evaluateString(scope, "o.voidFunction()", "test.js", 1, null));
    }

    private static class AnnotatedClass {
        @NodeFunction
        public void voidFunction() {
        }

        @NodeFunction
        public String overloadedFunction() {
            return "0";
        }

        @NodeFunction
        public String overloadedFunction(Object o1) {
            return "1";
        }

        @NodeFunction
        public String overloadedFunction(Object o1, Object o2) {
            return "2";
        }

        @NodeFunction
        public String overloadedFunction(Object o1, Object o2, Object o3) {
            return "3";
        }

        @NodeFunction
        public String numberParameter(Number n) {
            return String.format("%.2f", n.doubleValue());
        }

        @NodeFunction
        public String booleanParameter(Boolean b) {
            return b.toString();
        }

        @NodeFunction
        public String stringParameter(String s) {
            return s;
        }

        @NodeFunction
        public String intFunction(int i) {
            return Integer.toString(i);
        }

        @NodeFunction
        public String floatFunction(float i) {
            return String.format("%.3f", i);
        }
    }
}

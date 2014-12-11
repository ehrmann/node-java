package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.runtime.NodeFunction;
import com.davidehrmann.nodejava.script.AdapterFactory;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.Scriptable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class RhinoAdapterFactoryTest {

    @RunWith(Parameterized.class)
    public static class ToJavaTest {
        @Parameterized.Parameters
        public static Object[][] params() {
            return new Object[][]{
                    {Matchers.equalTo(Collections.emptyMap()), "(function(){return {}})()"},
                    {Matchers.equalTo(Collections.emptyList()), "(function(){return []})()"},
                    {Matchers.equalTo(ImmutableMap.of(
                            "string", "value",
                            "number", 1.0,
                            "array", Arrays.asList("string", 1.0, true),
                            0, true
                    )),"x = {string:'value', number: 1, array: ['string', 1, true]}; x[0] = true; x"},
                    {Matchers.equalTo(Collections.emptyList()), "[]"},
                    {Matchers.equalTo(Arrays.asList(0.0, true, "string")), "[0, true, 'string']"},
                    {Matchers.nullValue(), "null"},
                    {Matchers.sameInstance(AdapterFactory.JAVA_UNDEFINED), "(function(){})()"},
            };
        }

        private final Matcher matcher;
        private final String jsStatement;

        public ToJavaTest(Matcher matcher, String jsStatement) {
            this.matcher = matcher;
            this.jsStatement = Objects.requireNonNull(jsStatement);
        }

        @Test
        public void testToJava() {
            Context cx = Context.enter();
            RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);
            Scriptable scope = cx.initSafeStandardObjects();

            Object result = cx.evaluateString(scope, jsStatement, "testToJava.js", 1, null);
            assertThat(adapterFactory.toJava(result), matcher);
        }
    }

    @RunWith(Parameterized.class)
    public static class FromJavaTest {

        @Parameterized.Parameters
        public static Object[][] params() {
            return new Object[][] {
                    {true, IdScriptableObject.class, true, "x"},
                    {4.0, IdScriptableObject.class, (short) 4, "x"},
                    {"a", CharSequence.class, 'a', "x"},
                    {4.0, IdScriptableObject.class, 4, "x"},
                    {4.0, IdScriptableObject.class, (long) 4, "x"},
                    {4.0, IdScriptableObject.class, 4.0f, "x"},
                    {4.0, IdScriptableObject.class, 4.0, "x"},

                    {"value", MapAdapter.class, Collections.singletonMap("key", "value"), "x.key"},
                    {3.0, ListAdapter.class, Arrays.asList(1, 2, 3), "x[2]"},

                    {3.0, ObjectArrayAdapter.class, new Object[] { "1", 2, 3 }, "x[2]"},
                    {true, BooleanArrayAdapter.class, new boolean[] { false, false, true }, "x[2]"},
                    {"abc", CharArrayAdapter.class, new char[] { 'a', 'b', 'c' }, "x.toString()"},
                    {3.0, ShortArrayAdapter.class, new short[] { 1, 2, 3 }, "x[2]"},
                    {3.0, IntArrayAdapter.class, new int[] { 1, 2, 3 }, "x[2]"},
                    {3.0, LongArrayAdapter.class, new long[] { 1, 2, 3 }, "x[2]"},
                    {3.0, FloatArrayAdapter.class, new float[] { 1, 2, 3 }, "x[2]"},
                    {3.0, DoubleArrayAdapter.class, new double[] { 1, 2, 3 }, "x[2]"},

                    // TODO: implement ByteArrayAdapter
                    // {-255.0, ByteArrayAdapter.class, new byte[] { 0x00, (byte) 0xff, 0x80 }, "x[2]"},

                    {4.0, ObjectAdapter.class, new AnnotatedClass(), "x.value()"},
                    {4.0, FunctionAdapter.class, IDENTITY_FUNCTION, "x(4)"}
            };
        }

        private final Object expected;
        private final Class<?> expectedClass;
        private final Object javaObj;
        private final String jsStatement;

        public FromJavaTest(Object expected, Class<?> expectedClass, Object javaObj, String jsStatement) {
            this.expected = expected;
            this.expectedClass = expectedClass;
            this.javaObj = javaObj;
            this.jsStatement = jsStatement;
        }

        @Test
        public void testResultClass() {
            if (expected != null) {
                Context cx = Context.enter();
                RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);
                assertThat(adapterFactory.fromJava(javaObj), Matchers.instanceOf(expectedClass));
            }
        }

        @Test
        public void testResult() {
            Context cx = Context.enter();
            RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);
            Scriptable scope = cx.initSafeStandardObjects();
            scope.put("x", scope, adapterFactory.fromJava(javaObj));

            Object result = cx.evaluateString(scope, jsStatement, "testFromJava.js", 1, null);

            assertEquals(expected, adapterFactory.toJava(result));
        }
    }


    @RunWith(Parameterized.class)
    public static class JavaAdapterCommutivityTest {

        @Parameterized.Parameters
        public static Object[][] params() throws Exception {
            return new Object[][] {
                    {Collections.singletonMap("key", "value")},
                    {Arrays.asList(1, 2, 3)},

                    {new Object[] { "1", 2, 3 }},
                    {new boolean[] { false, false, true }},
                    {new char[] { 'a', 'b', 'c' }},
                    {new short[] { 1, 2, 3 }},
                    {new int[] { 1, 2, 3 }},
                    {new long[] { 1, 2, 3 }},
                    {new float[] { 1, 2, 3 }},
                    {new double[] { 1, 2, 3 }},

                    // TODO: implement ByteArrayAdapter
                    // {-255.0, ByteArrayAdapter.class, new byte[] { 0x00, (byte) 0xff, 0x80 }, "x[2]"},

                    {new AnnotatedClass()},
                    {AnnotatedClass.class.getMethod("value")},
                    {IDENTITY_FUNCTION}
            };
        }

        private final Object javaObj;

        public JavaAdapterCommutivityTest(Object javaObj) {
            this.javaObj = javaObj;
        }

        @Test
        public void testCommutivity() {
            Context cx = Context.enter();
            RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);
            Scriptable scope = cx.initSafeStandardObjects();
            scope.put("x", scope, adapterFactory.fromJava(javaObj));

            Object result = cx.evaluateString(scope, "x", "testFromJava.js", 1, null);

            assertSame(javaObj, adapterFactory.toJava(result));
        }
    }

    @RunWith(Parameterized.class)
    public static class RhinoAdapterCommutivityTest {

        @Parameterized.Parameters
        public static Object[][] params() throws Exception {
            return new Object[][] {
                    {"(function(){return [0]})()"},
                    {"(function(){return {key: 'value'}})()"},
                    {"(function(){return function(){}})()"},
                    {"(function(){return Math})()"},
            };
        }

        private final String jsStatement;

        public RhinoAdapterCommutivityTest(String jsStatement) {
            this.jsStatement = Objects.requireNonNull(jsStatement);
        }

        @Test
        public void testCommutivity() {
            Context cx = Context.enter();
            RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);
            Scriptable scope = cx.initSafeStandardObjects();

            Object result = cx.evaluateString(scope, jsStatement, "testFromJava.js", 1, null);

            assertSame(result, adapterFactory.fromJava(adapterFactory.toJava(result)));
        }

    }

    private static class AnnotatedClass {
        @NodeFunction
        public int value() { return 4; }
    }

    private static final Function<Object[], Object> IDENTITY_FUNCTION = params -> params[0];
}

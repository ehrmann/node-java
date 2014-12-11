package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.script.AdapterFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.collection.IsIn;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AbstractArrayAdapterTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Context cx = Context.enter();
    private final Scriptable scope = cx.initSafeStandardObjects();

    private final RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);

    @SuppressWarnings("unchecked")
    private final Function<Object, Object> coerceMock = mock(Function.class);

    private final int[] array = new int[10];

    public AbstractArrayAdapterTest() {
        Random random = new Random(42);
        for (int i = 0; i < array.length; ++i) {
            array[i] = random.nextInt();
        }

        AbstractArrayAdapter<int[]> adaptedArray = new AbstractArrayAdapter<int[]>(adapterFactory, scope, array) {
            @Override
            protected Object coerce(Object value) throws IllegalArgumentException {
                return coerceMock.apply(value);
            }
        };

        scope.put("array", scope, adaptedArray);
    }

    @Test
    public void testIsArray() {
        assertTrue((Boolean) cx.evaluateString(scope, "Array.isArray(array)", "test.js", 1, null));
        assertTrue((Boolean) cx.evaluateString(scope, "Array.prototype.isPrototypeOf(array)", "test.js", 1, null));
        assertTrue((Boolean) cx.evaluateString(scope, "array instanceof Array", "test.js", 1, null));
    }

    @Test
    public void testLength() {
        Object result = cx.evaluateString(scope, "array.length", "test.js", 1, null);
        assertEquals((double) array.length, ((Number) result).doubleValue(), 0.0);
    }

    @Test
    public void testValues() {
        String joined = cx.evaluateString(scope, "array.join(',')", "test.js", 1, null).toString();
        assertArrayEquals(
                array,
                Arrays.stream(joined.split(",")).mapToInt(Integer::new).toArray()
        );
    }

    @Test
    public void testGetStringIndex() {
        String value = cx.evaluateString(scope, "array['0']", "test.js", 1, null).toString();
        assertEquals(array[0], Integer.parseInt(value));
    }

    @Test
    public void testGet() {
        Object result = cx.evaluateString(scope, "array[0]", "test.js", 1, null);
        Number number = (Number) adapterFactory.toJava(result);
        assertEquals((double) array[0], number.doubleValue(), 0.0);
    }

    @Test
    public void testGetOutOfRange() {
        assertSame(AdapterFactory.JAVA_UNDEFINED, adapterFactory.toJava(cx.evaluateString(scope, "array[-1]", "test.js", 1, null)));
        assertSame(AdapterFactory.JAVA_UNDEFINED, adapterFactory.toJava(cx.evaluateString(scope, "array[10]", "test.js", 1, null)));
    }

    @Test
    public void testAssignment() {
        doReturn(-1).when(coerceMock).apply(any(Number.class));
        cx.evaluateString(scope, "array[0] = -1", "test.js", 1, null);
        assertEquals(-1, array[0]);
        verify(coerceMock).apply(argThat(IsIn.isOneOf(new Object[]{-1, -1.0})));
        verifyNoMoreInteractions(coerceMock);
    }

    @Ignore
    @Test
    public void testAssignmentOutOfRange() {
        cx.evaluateString(scope, "array[-1] = 1", "test.js", 1, null);
        cx.evaluateString(scope, "array[10] = 1", "test.js", 1, null);
        verifyNoMoreInteractions(coerceMock);
    }

    @Test
    public void testEnumeration() throws IOException {
        assertEquals(
                IntStream.range(0, array.length).mapToObj(String::valueOf).collect(Collectors.toList()),
                adapterFactory.toJava(cx.evaluateString(scope, "Object.keys(array)", "test.js", 1, null))
        );

        String json = cx.evaluateString(scope, "JSON.stringify(array)", "test.js", 1, null).toString();
        assertArrayEquals(OBJECT_MAPPER.readValue(json, int[].class), array);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(
                Arrays.stream(array).mapToObj(Integer::toString).collect(Collectors.joining(",")),
                cx.evaluateString(scope, "array.toString()", "test.js", 1, null).toString()
        );
    }

    @Test
    public void testSeal() throws Exception {
        doReturn(-1).when(coerceMock).apply(-1.0);
        cx.evaluateString(scope, "Object.seal(array); array[0] = -1;", "test.js", 1, null);
        assertNotEquals(-1, array[0]);
    }
}

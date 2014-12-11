package com.davidehrmann.nodejava.script.rhino;

import org.junit.Test;
import org.mockito.Mockito;
import org.mozilla.javascript.Scriptable;

import static org.junit.Assert.assertEquals;

public class FloatArrayAdapterTest {
    @Test
    public void testCoercion() {
        FloatArrayAdapter adapter = new FloatArrayAdapter(
                Mockito.mock(RhinoAdapterFactory.class),
                Mockito.mock(Scriptable.class), new float[0]
        );

        assertEquals(1.0f, (Float) adapter.coerce("resolveDependencies"), 0.0f);
        assertEquals(0.0f, (Float) adapter.coerce(null), 0.0f);
        assertEquals(0.0f, (Float) adapter.coerce(false), 0.0f);
        assertEquals(1.0f, (Float) adapter.coerce(true), 0.0f);
    }
}

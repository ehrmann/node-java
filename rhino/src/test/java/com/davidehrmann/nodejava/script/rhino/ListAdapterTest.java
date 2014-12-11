package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.runtime.NodeFunction;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ListAdapterTest {
    private final Context cx = Context.enter();
    private final Scriptable scope = cx.initSafeStandardObjects();
    private final RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);

    private final Object adapted = new Object() {
        @NodeFunction
        public String test() {
            return "test";
        }
    };

    @Test
    public void testToString() {
        List<Object> list = Arrays.asList(new Object[] {"0", "resolveDependencies", null});
        scope.put("list", scope, new ListAdapter<>(adapterFactory, cx.initStandardObjects(), list));
        assertEquals("0,resolveDependencies,", cx.evaluateString(scope, "list.toString()", "test.js", 1, null).toString());
    }

    @Test
    public void testSealing() {
        List<Object> list = new ArrayList<>(Arrays.asList(new Object[] {"0", "1"}));
        scope.put("list", scope, new ListAdapter<>(adapterFactory, cx.initStandardObjects(), list));
        cx.evaluateString(scope, "list[1] = -1; list[2] = -2; Object.seal(list); list[0] = null; list[1] = null; list[2] = null;", "test.js", 1, null);
        assertEquals(Arrays.asList(new Object[] { 0.0, -1.0, -2.0 }), list);
    }

    @Test
    public void testAdapting() {
        Map<Object, Object> map = new HashMap<>();
        map.put("adapted", adapted);
        scope.put("map", scope, new MapAdapter(adapterFactory, cx.initStandardObjects(), map));

        assertEquals("test", cx.evaluateString(scope, "map.adapted.test();", "test.js", 1, null).toString());
    }

    @Test
    public void testUnwrapping() {
        Map<Object, Object> map = new HashMap<>();
        map.put("adapted", adapted);
        scope.put("map", scope, new MapAdapter(adapterFactory, cx.initStandardObjects(), map));

        cx.evaluateString(scope, "map.adaptedCopy = map.adapted;", "test.js", 1, null);

        assertSame(adapted, map.get("adaptedCopy"));
    }

    // TODO: foreach test
}

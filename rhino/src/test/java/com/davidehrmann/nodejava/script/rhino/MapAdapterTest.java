package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.runtime.NodeFunction;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MapAdapterTest {
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
    public void testExistingStringKey() {
        Map<Object, Object> map = singletonMap("resolveDependencies", "bar");
        scope.put("map", scope, new MapAdapter(adapterFactory, cx.initStandardObjects(), map));
        assertEquals("bar", cx.evaluateString(scope, "map.resolveDependencies", "test.js", 1, null).toString());
    }

    @Test
    public void testExistingDoubleKey() {
        Map<Object, Object> map = singletonMap(0.0, "bar");
        scope.put("map", scope, new MapAdapter(adapterFactory, cx.initStandardObjects(), map));
        assertEquals("bar", cx.evaluateString(scope, "map[0]", "test.js", 1, null).toString());
    }

    @Test
    public void testToString() {
        Map<Object, Object> map = Collections.emptyMap();
        scope.put("map", scope, new MapAdapter(adapterFactory, cx.initStandardObjects(), map));
        assertEquals("[object Object]", cx.evaluateString(scope, "map.toString()", "test.js", 1, null).toString());
    }

    @Test
    public void testSealing() {
        Map<Object, Object> map = new HashMap<>();
        map.put("existingKey", "original");
        map.put(0.0, "original int");

        scope.put("map", scope, new MapAdapter(adapterFactory, cx.initStandardObjects(), map));
        cx.evaluateString(scope, "map.newKey = 'newValue'; map[1] = 'new int'; Object.seal(map); map.existingKey = 'touched'; map[0] = 'touched int'; map.sealedKey = 'fail'; map[3] = 'fail';", "test.js", 1, null);
        assertEquals(
                ImmutableMap.of(
                        "existingKey", "touched",
                        "newKey", "newValue",
                        0.0, "touched int",
                        1.0, "new int"
                ),
                map
        );
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

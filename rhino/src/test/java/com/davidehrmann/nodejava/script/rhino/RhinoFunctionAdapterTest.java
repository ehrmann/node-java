package com.davidehrmann.nodejava.script.rhino;

import com.davidehrmann.nodejava.runtime.NodeFunction;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@SuppressWarnings("unchecked")
public class RhinoFunctionAdapterTest {

    private final Object adapted = new Object() {
        @NodeFunction
        public String test() {
            return "test";
        }
    };

    private final Context cx = Context.enter();
    private final Scriptable scope = cx.initSafeStandardObjects();
    private final RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(cx);

    @Test
    public void testSimpleFunction() {
        Object result = cx.evaluateString(scope, "function f() { return 10 }; f", "test.js", 1, null);
        //assertThat(result, Matchers.instanceOf(org.mozilla.javascript.Function.class));
        Function<Object[], Object> function = new RhinoFunctionAdapter(adapterFactory, (org.mozilla.javascript.Function) result);
        assertEquals(10.0, function.apply(new Object[0]));
    }

    @Test
    public void testParamAdapting() {
        Object result = cx.evaluateString(scope, "function f(d) { return d.test() }; f", "test.js", 1, null);
        //assertThat(result, Matchers.instanceOf(org.mozilla.javascript.Function.class));
        Function<Object[], Object> function = new RhinoFunctionAdapter(adapterFactory, (org.mozilla.javascript.Function) result);
        assertEquals("test", function.apply(new Object[] { adapted }));
    }

    @Test
    public void testParamUnwrapping() {
        Object result = cx.evaluateString(scope, "function f(d) { return d }; f", "test.js", 1, null);
        //assertThat(result, Matchers.instanceOf(org.mozilla.javascript.Function.class));
        Function<Object[], Object> function = new RhinoFunctionAdapter(adapterFactory, (org.mozilla.javascript.Function) result);
        assertSame(adapted, function.apply(new Object[] { adapted }));
    }
}

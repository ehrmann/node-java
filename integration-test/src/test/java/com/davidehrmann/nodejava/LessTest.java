package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.script.CompilationException;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LessTest extends SqlitePackageManagerTest {

    public LessTest() throws PackageManagerException, NoMatchingPackageException, FileStoreException, IOException, CompilationException {
        super("less", VersionSpec.fromString("2.x"), "/main.js");
    }

    @Test
    public void testLess() throws IOException, PackageManagerException, NoMatchingPackageException, FileStoreException, InterruptedException, ExecutionException, TimeoutException {
        String css = Joiner.on("\n").join(
                "@color-base: #2d5e8b;",
                ".class1 {",
                "  background-color: @color-base;",
                "}"
        );

        String expected = Joiner.on("\n").join(
                ".class1 {",
                "  background-color: #2d5e8b;",
                "}",
                ""
        );

        @SuppressWarnings("unchecked")
        Map<Object, Object> less = (Map<Object, Object>)module.require("less");
        final Function render = (Function)less.get("render");

        final SettableFuture<Object> result = SettableFuture.create();
        Function callback = new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                result.set(((Scriptable) args[1]).get("css", (Scriptable)args[1]));
                return Context.getUndefinedValue();
            }
        };

        render.call(context, context.initStandardObjects(), (Scriptable)less, new Object[] { css, callback });
        Assert.assertEquals(expected, result.get(1, TimeUnit.SECONDS));
    }
}

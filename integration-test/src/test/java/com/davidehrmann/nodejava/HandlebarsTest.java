package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.script.CompilationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HandlebarsTest extends SqlitePackageManagerTest {

    public HandlebarsTest() throws PackageManagerException, NoMatchingPackageException, FileStoreException, IOException, CompilationException {
        super("handlebars", VersionSpec.fromString("3.x"), "main.js");
    }

    @Test
    public void testHandlebars() throws IOException, PackageManagerException, NoMatchingPackageException, FileStoreException {
        String source = "<p>Hello, my name is {{name}}. I am from {{hometown}}. I have " +
                "{{kids.length}} kids:</p>" +
                "<ul>{{#kids}}<li>{{name}} is {{age}}</li>{{/kids}}</ul>";

        Map<String, ?> data = ImmutableMap.of(
                "name", "Alan",
                "hometown", "Somewhere, TX",
                "kids", ImmutableList.of(
                        ImmutableMap.of(
                                "name", "Jimmy",
                                "age", "12"
                        ),
                        ImmutableMap.of(
                                "name", "Sally",
                                "age", "4"
                        )
                )
        );

        String expected = "<p>Hello, my name is Alan. I am from Somewhere, TX. I have 2 kids:</p><ul><li>Jimmy is 12</li><li>Sally is 4</li></ul>";



        @SuppressWarnings("unchecked")
        Map<Object, Object> handlebars = (Map<Object, Object>) super.module.require("handlebars");
        Function compile = (Function) handlebars.get("compile");

        Context cx = ContextFactory.getGlobal().enterContext();
        try {
            Scriptable scope = cx.initStandardObjects();
            Object json = adapterFactory.fromJava(data);
            Function template = (Function)compile.call(cx, scope, null, new Object[] { source });
            Object result = template.call(cx, cx.initStandardObjects(), null, new Object[] { json });
            assertEquals(expected, result);
        } finally {
            Context.exit();
        }
    }
}

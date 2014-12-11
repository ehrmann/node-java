package com.davidehrmann.nodejava;

import com.google.common.collect.Iterators;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class NodePathResolutionStrategyTest {
    @Test
    public void testFoo() {
        NodePathResolutionStrategy s = new NodePathResolutionStrategy();
        String[] actual = Iterators.toArray(s.getLookupQueue("/home/ry/projects", "bar.js"), String.class);
        assertArrayEquals(new String[] {
                "/home/ry/projects/node_modules/bar.js",
                "/home/ry/node_modules/bar.js",
                "/home/node_modules/bar.js",
                "/node_modules/bar.js",
        }, actual);
    }

    @Test
    public void testRelativePath() {
        NodePathResolutionStrategy s = new NodePathResolutionStrategy();

        String[] actual = Iterators.toArray(s.getLookupQueue("/node_modules/handlebars/lib/", "../dist/cjs/handlebars"), String.class);
        assertArrayEquals(new String[] {
                "/node_modules/handlebars/dist/cjs/handlebars.js",
                "/node_modules/handlebars/dist/cjs/handlebars/index.js",
                "/node_modules/handlebars/dist/cjs/handlebars/package.json",
                "/node_modules/handlebars/dist/cjs/handlebars",
        }, actual);
    }
}
package com.davidehrmann.nodejava.scriptloader;

import com.davidehrmann.nodejava.SimplePathResolutionStrategy;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class MemoizedResolvingScriptLoaderTest {
    @Test
    public void testDirectorySearch() throws IOException {
        ScriptCompiler<?> compiler = Mockito.mock(ScriptCompiler.class);

        ScriptLoader scriptLoader = Mockito.mock(ScriptLoader.class);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(scriptLoader.loadScript(argumentCaptor.capture())).thenReturn(null);

        ResolvingScriptLoader resolvingScriptLoader = new MemoizedResolvingScriptLoader<>(scriptLoader, SimplePathResolutionStrategy.INSTANCE, compiler);
        resolvingScriptLoader.loadScript("bar.js", "/home/ry/projects");

        assertArrayEquals(new String[]{
                "/home/ry/projects/node_modules/bar.js",
                "/home/ry/node_modules/bar.js",
                "/home/node_modules/bar.js",
                "/node_modules/bar.js",
        }, argumentCaptor.getAllValues().toArray());
    }
}

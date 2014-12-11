package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.runtime.RuntimeBuilder;
import com.davidehrmann.nodejava.script.AdapterFactory;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.script.ScriptRunner;
import com.davidehrmann.nodejava.scriptloader.MemoizedResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ResolvingScriptLoader.ResolvedScript;
import com.davidehrmann.nodejava.scriptloader.ScriptLoader;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Useful for running bin commands?
 */
public class JNodeRunner<R, S> implements Callable<Integer> {

    protected final String main;
	protected final String[] mainArgs;

    protected final ScriptLoader scriptLoader;
	protected final ScriptCompiler<S> compiler;
	protected final ScriptRunner<R, S> runner;
    private final AdapterFactory<R> adapterFactory;

    public JNodeRunner(ScriptLoader scriptLoader, ScriptCompiler<S> compiler, ScriptRunner<R, S> runner, AdapterFactory<R> adapterFactory, String main, String[] mainArgs) {
        this.main = Objects.requireNonNull(main);
        this.mainArgs = Objects.requireNonNull(mainArgs);

		this.scriptLoader = Objects.requireNonNull(scriptLoader);
		this.compiler = Objects.requireNonNull(compiler);
		this.runner = Objects.requireNonNull(runner);
        this.adapterFactory = Objects.requireNonNull(adapterFactory);
    }

    public Integer call() throws Exception {
        ResolvingScriptLoader<S> mainLoader = new MemoizedResolvingScriptLoader<>(scriptLoader, new NodePathResolutionStrategy(), compiler);

        ResolvedScript<S> mainScript = mainLoader.loadScript(main, "/");
        if (mainScript == null) {
            // FIXME: correct error code
			// FIXME: console out
			return -1;
        }

		Map builtins = null; // Collections.<String, Object>singletonMap("console", console)

        Map<String, Object> runtime = new RuntimeBuilder()
                .withProcess(mainArgs)
                .build();

		Module<?, S> main = new Module<>(builtins, runner, mainLoader, mainScript);
		main.run();

        return 0;
    }


}

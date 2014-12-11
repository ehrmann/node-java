package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.runtime.Console;
import com.davidehrmann.nodejava.runtime.FS;
import com.davidehrmann.nodejava.runtime.Os;
import com.davidehrmann.nodejava.runtime.Path;
import com.davidehrmann.nodejava.runtime.Process;
import com.davidehrmann.nodejava.runtime.Url;
import com.davidehrmann.nodejava.runtime.Util;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptCompiler;
import com.davidehrmann.nodejava.scriptloader.ChainedScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ClasspathScriptLoader;
import com.davidehrmann.nodejava.scriptloader.GcjCoreScriptLoader;
import com.davidehrmann.nodejava.scriptloader.MemoizedResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ResolvingScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ScriptLoader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JNode {

    public static final String MAIN_PROPERTY_KEY = "JNodeRunner.main";
    public static final String SCRIPT_LOADER_PROPERTY_KEY = "JNodeRunner.scriptLoader";

    public static final String CLASS_LOADER_SCRIPT_LOADER = "ClassLoaderScriptLoader";
    public static final String CLASSPATH_SCRIPT_LOADER = "ClasspathScriptLoader";
    public static final String GCJ_CORE_SCRIPTLOADER = "GcjCoreScriptloader";

    public static final String DEFAULT_SCRIPT_LOADERS = CLASSPATH_SCRIPT_LOADER;

    public JNode(ScriptLoader scriptLoader, String main) {

    }

    public static void main(String[] args) throws IOException {

        String[] mainArgs;
        String main;

        // See if we have a main specified as a system property
        if ((main = System.getProperty(MAIN_PROPERTY_KEY)) != null) {
            ArrayList<String> temp = new ArrayList<>(args.length + 2);
            temp.add("node");
            temp.add(main);
            temp.addAll(Arrays.asList(args));

            mainArgs = temp.toArray(new String[0]);
        }
        // Otherwise, use the one in args
        else {
            if (args.length < 1) {
                usage(System.err);
                System.exit(-1);
                return;
            }

            main = args[0];

            ArrayList<String> temp = new ArrayList<>(args.length + 1);
            temp.add("node");
            temp.addAll(Arrays.asList(args));

            mainArgs = temp.toArray(new String[0]);
        }

        // Set up the ScriptLoader
        List<ScriptLoader> scriptLoaders = new ArrayList<>();
        for (String scriptLoaderName : System.getProperty(SCRIPT_LOADER_PROPERTY_KEY, DEFAULT_SCRIPT_LOADERS).split(",")) {
            if (CLASS_LOADER_SCRIPT_LOADER.equals(scriptLoaderName)) {
                //scriptLoaders.add(new ClassLoaderScriptLoader(JNode.class.getClassLoader(), new RhinoScriptCompiler(), Script.class));
            } else if (CLASSPATH_SCRIPT_LOADER.equals(scriptLoaderName)) {
                scriptLoaders.add(new ClasspathScriptLoader(JNode.class.getClassLoader()));
            } else if (GCJ_CORE_SCRIPTLOADER.equals(scriptLoaderName)) {
                scriptLoaders.add(new GcjCoreScriptLoader());
            } else {
                System.err.printf("Unrecognized ScriptLoader: %s\n", scriptLoaderName);
                usage(System.err);
                System.exit(-1);
                return;
            }
        }

        ScriptLoader scriptLoader;

        if (scriptLoaders.isEmpty()) {
            System.err.printf("No ScriptLoader");
            usage(System.err);
            System.exit(-1);
            return;
        } else if (scriptLoaders.size() == 1) {
            scriptLoader = scriptLoaders.get(0);
        } else {
            scriptLoader = new ChainedScriptLoader(scriptLoaders);
        }

        // Load main
        ResolvingScriptLoader mainLoader = new MemoizedResolvingScriptLoader(scriptLoader, SimplePathResolutionStrategy.INSTANCE, new RhinoScriptCompiler());

        ResolvingScriptLoader.ResolvedScript<Script> mainScript = mainLoader.loadScript(main, "/");
        if (mainScript == null) {
            System.err.printf("Main '%s' not found\n", main);
            usage(System.err);
            System.exit(-1);
        }

        // Build up the built-in environment.  At some point, this will probably get moved into a factory.
        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects();

        cx.getWrapFactory().setJavaPrimitiveWrap(false);

        Map<String, Object> env = new LinkedHashMap<String, Object>();

        env.put("fs", new FS());
        env.put("os", new Os());
        env.put("sys", new Util());
        env.put("path", new Path());
        env.put("util", new Util());
        env.put("url", new Url());

        //scope.put("require", scope, new Module(mainScript.getAbsoluteDir(), env, new MemoizedResolvingScriptLoader(scriptLoader, new JNodePathResolutionStrategy())));
        scope.put("process", scope, new Process(mainArgs));
        scope.put("console", scope, new Console());

        // Run main
        mainScript.getScript().exec(cx, scope);
    }

    public static void usage(PrintStream out) {
        out.printf("Usage: java -cp ... %s <main js> [args]\n", JNode.class.getCanonicalName());
        out.printf("  System properties:\n");
        out.printf("    -D%s=... - specify main by property.  <main js> must not be omitted\n", MAIN_PROPERTY_KEY);
        out.printf("    -D%s=... - specify search order for looking up JS files\n", SCRIPT_LOADER_PROPERTY_KEY);
        out.printf("      %s - Load resources from the classpath\n", CLASSPATH_SCRIPT_LOADER);
        out.printf("      %s - Load precomiled resources\n", CLASS_LOADER_SCRIPT_LOADER);
        out.printf("      %s - Load resources from core:/ URLs\n", GCJ_CORE_SCRIPTLOADER);
    }

}

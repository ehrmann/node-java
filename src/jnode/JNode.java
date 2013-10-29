package jnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import jnode.runtime.Console;
import jnode.runtime.FS;
import jnode.runtime.Mkdirp;
import jnode.runtime.Os;
import jnode.runtime.Path;
import jnode.runtime.Process;
import jnode.runtime.Url;
import jnode.runtime.Util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class JNode {

	public static final String JNODE_MAIN_PROPERTY_KEY = "jnode.JNode.main";
	public static final String JNODE_FILELOADER_PROPERTY_KEY = "jnode.JNode.fileloader";
	
	public static void main(String[] args) throws IOException {
		
		String[] mainArgs;
		String main;
		
		// See if we have a main specified as a system property
		if ((main = System.getProperty(JNODE_MAIN_PROPERTY_KEY)) != null) {
			ArrayList<String> temp = new ArrayList<String>(args.length + 2);
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
			
			ArrayList<String> temp = new ArrayList<String>(args.length + 1);
			temp.add("node");
			temp.addAll(Arrays.asList(args));
			
			mainArgs = temp.toArray(new String[0]);
		}
		
		// Set up the FileLoader
		FileLoader fileLoader;
		if ("GcjCoreFileLoader".equals(System.getProperty(JNODE_FILELOADER_PROPERTY_KEY))) {
			fileLoader = new GcjCoreFileLoader();
		} else if (System.getProperty(JNODE_FILELOADER_PROPERTY_KEY) != null) {
			fileLoader = new ClassloaderFileLoader(JNode.class.getClassLoader());
			System.err.printf("Unrecognized %s value: %s", JNODE_FILELOADER_PROPERTY_KEY, System.getProperty(JNODE_FILELOADER_PROPERTY_KEY));
		} else {
			fileLoader = new ClassloaderFileLoader(JNode.class.getClassLoader());
		}
		
		// Read in called script
		StringBuilder scriptBuilder = new StringBuilder();
		char[] buffer = new char[2048];
		InputStream in;
		
		inInit: {
			// First, try from the classloader
			in = JNode.class.getClassLoader().getResourceAsStream(main);
			if (in != null) {
				break inInit;
			}
		
			// Next, try a gcj core: URL
			try {
				in = (new URL("core:/" + main)).openStream();
				if (in != null) {
					break inInit;
				}
			} catch (MalformedURLException e) { }
		}
		
		if (in != null) {
			try {
				Reader reader = new InputStreamReader(in, "UTF-8");
				try {
					int read;
					while ((read = reader.read(buffer)) >= 0) {
						scriptBuilder.append(buffer, 0 , read);
					}
				} finally {
					reader.close();
				}
			} finally {
				in.close();
			}
		} else {
			System.err.printf("main js <%s> not fonud\n", main);
			usage(System.err);
			System.exit(-1);
			return;
		}
		
		// Comment out the #! (if present)
		String script = scriptBuilder.toString();
		if (script.startsWith("#!")) {
			script = "// " + script;
		}
		
		// Wrap the script so return is supported
		script = "(function() { " + script + "\n})();";
		
		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects();
		
		cx.getWrapFactory().setJavaPrimitiveWrap(false);
		
		Map<String, Object> env = new LinkedHashMap<String, Object>();

		env.put("fs", new FS(cx, scope));
		env.put("mkdirp", new Mkdirp());
		env.put("os", new Os());
		env.put("sys", new Util());
		env.put("path", new Path());
		env.put("util", new Util());
		env.put("url", new Url());
				
		scope.put("require", scope, new Require(Require.getPath("/", "/" + main), env, fileLoader));
		scope.put("process", scope, new Process(mainArgs));
		scope.put("console", scope, new Console());
		
		// Run the code
		cx.evaluateString(scope, script, main, 1, null);
	}
	
	public static void usage(PrintStream out) {
		out.printf("Usage: java -cp ... %s <main js> [args]\n", JNode.class.getCanonicalName());
	}

}

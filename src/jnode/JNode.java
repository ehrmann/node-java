package jnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import jnode.lib.FS;
import jnode.lib.Mkdirp;
import jnode.lib.Os;
import jnode.lib.Path;
import jnode.lib.Sys;
import jnode.lib.Url;
import jnode.lib.Util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class JNode {

	public static Map<String, Object> env;
	static {
		env = new LinkedHashMap<String, Object>();
		env.put("fs", new FS());
		env.put("mkdirp", new Mkdirp());
		env.put("os", new Os());
		env.put("sys", new Sys());
		env.put("path", new Path());
		env.put("util", new Util());
		env.put("url", new Url());
	}
	
	public static void main(String[] args) throws IOException {
		
		if (args.length < 1) {
			usage(System.err);
			System.exit(-1);
			return;
		
		}
		
		String main = args[0];
		
		// Read in called script
		StringBuilder scriptBuilder = new StringBuilder();
		char[] buffer = new char[2048];
		InputStream in = JNode.class.getClassLoader().getResourceAsStream(main);
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
		
		// Populate the default environment
		scope.put("require", scope, new Require(Require.getPath("/", "/" + main), env));
		scope.put("process", scope, new Process(args));
		scope.put("console", scope, new Console());
		
		// Run the code
		cx.evaluateString(scope, script, args[0], 1, null);
	}
	
	public static void usage(PrintStream out) {
		out.printf("Usage: java -cp ... %s <main js> [args]\n", JNode.class.getCanonicalName());
	}

}

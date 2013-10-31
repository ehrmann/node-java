package jnode;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;

public class AbstractCachingScriptLoader implements ScriptLoader {

	
	
	public AbstractCachingScriptLoader(ScriptLoader scriptLoader) {
		
	}
	
	@Override
	public Script loadScript(String path) {
		String fullFilePath = path;
		
		// Get a canonical name for this entry
		String path = getPath(this.pwd, fullFilePath);
		String file = getfile(fullFilePath);
		fullFilePath = path + file;
		
		// Serve this out of the cache if possible
		if (this.cache.containsKey(fullFilePath)) {
			return this.cache.get(fullFilePath);
		}
		
		String resourceName;
		InputStream in;
		builder: {
			String classpathRelativeFilePath = fullFilePath.substring(1);
			
			if (classpathRelativeFilePath.endsWith(".js")) {
				resourceName = classpathRelativeFilePath;
				if (this.cache.containsKey(resourceName)) {
					return this.cache.get(resourceName);
				}
				
				in = this.fileLoader.getInputStream(resourceName);
				if (in != null) {
					break builder;
				}
			} else {
				resourceName = classpathRelativeFilePath + ".js";
				if (this.cache.containsKey(resourceName)) {
					return this.cache.get(resourceName);
				}
				
				in = this.fileLoader.getInputStream(resourceName);
				if (in != null) {
					break builder;
				}
				
				resourceName = classpathRelativeFilePath + "/index.js";
				if (this.cache.containsKey(resourceName)) {
					return this.cache.get(resourceName);
				}
				
				in = this.fileLoader.getInputStream(resourceName);
				if (in != null) {
					path = path + file + "/";
					file = "index.js";
					break builder;
				}
				
				resourceName = null;
				// TODO JSON
			}
			
			
		}
	}
	
	static final String getPath(String pwd, String file) throws JavaScriptException {
		if (!pwd.startsWith("/")) {
			throw new IllegalArgumentException("pwd isn't absolute");
		} 
		
		file = file.replaceFirst("/[^/]*$", "");
		
		LinkedList<String> pwdStack = new LinkedList<String>();
		
		if (!file.startsWith("/") && !file.isEmpty()) {
			pwdStack.addAll(Arrays.asList(pwd.split("/")));
		}
		
		pwdStack.addAll(Arrays.asList(file.split("/")));

		
		ListIterator<String> i = pwdStack.listIterator();
		
		while (i.hasNext()) {
			String dir = i.next();
			if (dir.isEmpty()) {
				i.remove();
			} else if (".".equals(dir)) {
				i.remove();
			} else if ("..".equals(dir)) {
				i.remove();
				
				if (!i.hasPrevious()) {
					throw new JavaScriptException("../ trying to escape sandbox", "require", 4);
				}
				
				i.previous();
				i.remove();
			}
		}

		StringBuilder sb = new StringBuilder(pwd.length() * 2);
		sb.append('/');
		
		for (String dir : pwdStack) {
			sb.append(dir).append('/');
		}
		
		return sb.toString();
	}
	
	static final String getfile(String file) {
		return file.replaceFirst(".*[/]", "");
	}

}

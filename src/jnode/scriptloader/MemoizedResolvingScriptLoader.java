package jnode.scriptloader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import jnode.PathResolutionStrategy;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;

public class MemoizedResolvingScriptLoader implements ResolvingScriptLoader {

	protected final Map<String, ResolvedScript> memos = new HashMap<String, ResolvedScript>();
	protected final ScriptLoader scriptLoader;
	protected final PathResolutionStrategy pathResolutionStrategy;
	
	public MemoizedResolvingScriptLoader(ScriptLoader scriptLoader, PathResolutionStrategy pathResolutionStrategy) {
		if (scriptLoader == null) {
			throw new NullPointerException("scriptLoader was null");
		}
		if (pathResolutionStrategy == null) {
			throw new NullPointerException("pathResolutionStrategy was null");
		}
		
		this.scriptLoader = scriptLoader;
		this.pathResolutionStrategy = pathResolutionStrategy;
	}
	
	public ResolvedScript loadScript(String relativePath, String pwd) {
		
		// Get a canonical name for this entry
		String absoluteDir = getDir(pwd, relativePath);
		String file = getFile(relativePath);
		String absolutePath = absoluteDir + file;
		
		// Serve this out of the memos if possible
		if (this.memos.containsKey(absolutePath)) {
			return this.memos.get(absolutePath);
		}
		
		ResolvedScript result = null;
		try {
			Iterator<String> i = this.pathResolutionStrategy.getLookupQueue(absolutePath);
			while (i.hasNext()) {
				String nextPath = i.next();
				Script script = scriptLoader.loadScript(nextPath);
				if (script != null) {
					result = new DefaultResolvedScript(script, getDir("/", nextPath), getFile(nextPath));
					break;
				}
			}
		} finally {
			this.memos.put(absolutePath, result);
		}
		
		return result;
	}
	
	protected static final String getDir(String pwd, String path) throws JavaScriptException {
		if (!pwd.startsWith("/")) {
			throw new IllegalArgumentException("pwd isn't absolute");
		} 
		
		path = path.replaceFirst("/[^/]*$", "");
		
		LinkedList<String> pwdStack = new LinkedList<String>();
		
		if (!path.startsWith("/") && !path.isEmpty()) {
			pwdStack.addAll(Arrays.asList(pwd.split("/")));
		}
		
		pwdStack.addAll(Arrays.asList(path.split("/")));

		
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
					// FIXME: This probably shouldn't be a JS exception
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
	
	protected static final String getFile(String path) {
		return path.replaceFirst(".*[/]", "");
	}

}

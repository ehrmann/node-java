package jnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class Require extends BaseFunction {
	
	private static final long serialVersionUID = -8413606781796676262L;
	
	protected final String pwd;
	protected final FileLoader fileLoader;
	private final Map<String, Object> cache;
	
	public Require(String initalPwd, Map<String, Object> builtins, FileLoader fileLoader) {
		if (builtins == null) {
			throw new NullPointerException("builtins was null");
		}
		if (fileLoader == null) {
			throw new NullPointerException("fileLoader was null");
		}

		this.cache = new HashMap<String, Object>();
		this.pwd = initalPwd;
		this.fileLoader = fileLoader;
		
		this.cache.putAll(builtins);

	}
	
	private Require(Require old, String pwd) {
		if (old == null) {
			throw new RuntimeException("old was null");
		}
		if (pwd == null) {
			throw new RuntimeException("pwd was null");
		}
		
		this.cache = old.cache;
		this.pwd = pwd;
		this.fileLoader = old.fileLoader;
	}
	

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1 && args[0] != null) {
			String fullFilePath = args[0].toString();
			
			// If the require doesn't have a path (i.e. a builtin), try pulling it directly from cache
			if (!fullFilePath.startsWith("/") && !fullFilePath.startsWith("./") && !fullFilePath.startsWith("../")) {
				//return /*Context.toObject(*/this.cache.get(args[0].toString())/*, scope)*/;
				return Context.toObject(this.cache.get(args[0].toString()), scope);
			}
			
			// Get a canonical name for this entry
			String path = getPath(this.pwd, fullFilePath);
			String file = getfile(fullFilePath);
			fullFilePath = path + file;
			
			// Serve this out of the cache if possible
			if (this.cache.containsKey(fullFilePath)) {
				return this.cache.get(fullFilePath);
			}
			
			StringBuilder scriptBuilder = new StringBuilder(4096);
			char[] buffer = new char[2048];
						
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
			
			if (in != null) {
				try {
					// Try loading the file as a class
			        String baseName = "c";
			        if (resourceName.length() > 0) {
			        	baseName = resourceName.replaceAll("\\W", "_");
			        	if (!Character.isJavaIdentifierStart(baseName.charAt(0))) {
			        		baseName = "_" + baseName;
			        	}
			        }
			        
			        try {
						Class<?> jsClass = Class.forName("org.mozilla.javascript.gen." + baseName);
						if (Script.class.isAssignableFrom(jsClass)) {
							@SuppressWarnings("unchecked")
							Class<Script> jsScriptClass = (Class<Script>)jsClass;
							Script script = jsScriptClass.newInstance();
							
							Scriptable exportsScope = cx.newObject(scope);
							exportsScope.setParentScope(scope);
							Object r = cx.newObject(scope);
							exportsScope.put("exports", exportsScope, r);
							exportsScope.put("require", exportsScope, new Require(this, path));
							this.cache.put(resourceName, r);
							script.exec(cx, exportsScope);
							System.err.println("Loaded " + resourceName + " from class");
							return r;
						}
					} catch (ClassNotFoundException e) {
						System.err.println("Failed to use precompiled class; " + e);
					} catch (InstantiationException e) {
						System.err.println("Failed to use precompiled class; " + e);
					} catch (IllegalAccessException e) {
						System.err.println("Failed to use precompiled class; "+ e);
					}
			        

			        
			        // DONE
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
				} catch (IOException e) {
					throw new JavaScriptException(e, "require", 2);
				}
				
				Scriptable exportsScope = cx.newObject(scope);
				exportsScope.setParentScope(scope);
				Object r = cx.newObject(scope);
				exportsScope.put("exports", exportsScope, r);
				exportsScope.put("require", exportsScope, new Require(this, path));
				this.cache.put(resourceName, r);
				cx.evaluateString(exportsScope, scriptBuilder.toString(), "require", 10, null);
				return r;
			} else {
				throw new JavaScriptException(path + file + " not found", "require", 1);
			}		
		}
		
		return Context.getUndefinedValue();
	}

	@Override
	public Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		// TODO Auto-generated method stub
		return null;
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

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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

public class Require implements Function {
	
	protected final String pwd;
	private Map<String, Object> cache = new HashMap<String, Object>();
	
	public Require(String initalPwd, Map<String, Object> builtins) {
		if (builtins == null) {
			throw new NullPointerException("builtins was null");
		}
		try {
			this.pwd = initalPwd;
			this.cache.putAll(builtins);
		} catch (JavaScriptException e) {
			throw new IllegalArgumentException("initialPwd wasn't an absolute path");
		}
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
	}
	
	@Override
	public void delete(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDefaultValue(Class<?> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getParentScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParentScope(Scriptable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrototype(Scriptable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1 && args[0] != null) {
			String fullFilePath = args[0].toString();
			
			// If the require doesn't have a path (i.e. a builtin), try pulling it directly from cache
			if (!fullFilePath.startsWith("/") && !fullFilePath.startsWith("./") && !fullFilePath.startsWith("../")) {
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
			
			String resourceName;
			InputStream in;
			builder: {
				String classpathRelativeFilePath = fullFilePath.substring(1);
				
				if (classpathRelativeFilePath.endsWith(".js")) {
					resourceName = classpathRelativeFilePath;
					if (this.cache.containsKey(resourceName)) {
						return this.cache.get(resourceName);
					}
					
					in = JNode.class.getClassLoader().getResourceAsStream(resourceName);
					if (in != null) {
						break builder;
					}
				} else {
					resourceName = classpathRelativeFilePath + ".js";
					if (this.cache.containsKey(resourceName)) {
						return this.cache.get(resourceName);
					}
					
					in = JNode.class.getClassLoader().getResourceAsStream(resourceName);
					if (in != null) {
						break builder;
					}
					
					resourceName = classpathRelativeFilePath + "/index.js";
					if (this.cache.containsKey(resourceName)) {
						return this.cache.get(resourceName);
					}
					
					in = JNode.class.getClassLoader().getResourceAsStream(resourceName);
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
				StringBuilder scriptBuilder = new StringBuilder(4096);
				char[] buffer = new char[2048];
				try {
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

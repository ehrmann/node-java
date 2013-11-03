package jnode;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import jnode.scriptloader.ResolvingScriptLoader;
import jnode.scriptloader.ResolvingScriptLoader.ResolvedScript;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class Require extends BaseFunction {
	
	private static final long serialVersionUID = -8413606781796676262L;
	
	protected final String pwd;
	protected final ResolvingScriptLoader scriptLoader;
	private final Map<Script, Object> memos;
	private final Map<String, Object> builtins;
	
	public Require(String initalPwd, Map<String, Object> builtins, ResolvingScriptLoader scriptLoader) {
		if (builtins == null) {
			throw new NullPointerException("builtins was null");
		}
		if (scriptLoader == null) {
			throw new NullPointerException("scriptLoader was null");
		}

		this.memos = new IdentityHashMap<Script, Object>();
		this.pwd = initalPwd;
		this.scriptLoader = scriptLoader;
		this.builtins = new HashMap<String, Object>(builtins);

	}
	
	private Require(Require old, String pwd) {
		if (old == null) {
			throw new RuntimeException("old was null");
		}
		if (pwd == null) {
			throw new RuntimeException("pwd was null");
		}
		
		this.memos = old.memos;
		this.pwd = pwd;
		this.scriptLoader = old.scriptLoader;
		this.builtins = old.builtins;
	}
	

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length >= 1 && args[0] != null) {
			String fullFilePath = args[0].toString();
			
			// If this is a built-in, serve it
			if (this.builtins.containsKey(fullFilePath)) {
				return builtins.get(fullFilePath);
			}
			
			// Otherwise, look it up from the script loader
			ResolvedScript script = this.scriptLoader.loadScript(fullFilePath, this.pwd);
			if (script == null) {
				throw new JavaScriptException(fullFilePath + " not found", "require", 1);
			}
			
			// Serve this out of the cache if possible
			if (this.memos.containsKey(script)) {
				return this.memos.get(script);
			}
			
			// Evaluate the script and return the result
			Scriptable exportsScope = cx.newObject(scope);
			exportsScope.setParentScope(scope);
			Object r = cx.newObject(scope);
			exportsScope.put("exports", exportsScope, r);
			exportsScope.put("require", exportsScope, new Require(this, script.getAbsoluteDir()));
			this.memos.put(script, r);
			script.exec(cx, exportsScope);
			return r;
		}
		
		return Context.getUndefinedValue();
	}
	
	
	@Override
	public Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		return null;
	}
	

}

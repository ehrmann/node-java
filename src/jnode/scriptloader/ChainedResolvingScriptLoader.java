package jnode.scriptloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mozilla.javascript.Script;

public class ChainedResolvingScriptLoader implements ScriptLoader {

	protected final List<ScriptLoader> scriptLoaders;
	
	public ChainedResolvingScriptLoader(List<ScriptLoader> scriptLoaders) {
		if (scriptLoaders == null) {
			throw new NullPointerException("scriptLoaders was null");
		}
		
		this.scriptLoaders = Collections.unmodifiableList(new ArrayList<ScriptLoader>(scriptLoaders));
	}

	public Script loadScript(String absolutePath) {
		Script script = null;
		
		for (ScriptLoader scriptLoader : this.scriptLoaders) {
			script = scriptLoader.loadScript(absolutePath);
			if (script != null) {
				break;
			}
		}
		
		return script;
	}
	
}

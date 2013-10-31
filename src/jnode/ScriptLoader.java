package jnode;

import org.mozilla.javascript.Script;

public interface ScriptLoader {

	public Script loadScript(String path);
	
}

package jnode.scriptloader;

import org.mozilla.javascript.Script;

public interface ScriptLoader {

	public Script loadScript(String absolutePath);

}

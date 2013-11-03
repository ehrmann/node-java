package jnode.scriptloader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public interface ResolvingScriptLoader {
	public ResolvedScript loadScript(String relativePath, String pwd);
	
	
	public interface ResolvedScript extends Script {
		public String getFile();
		public String getAbsoluteDir();
	}
	
	public static class DefaultResolvedScript implements ResolvedScript {

		protected final Script script;
		protected final String file;
		protected final String dir;
		
		public DefaultResolvedScript(Script script, String dir, String file) {
			if (script == null) {
				throw new NullPointerException("script was null");
			}
			if (dir == null) {
				throw new NullPointerException("dir was null");
			}
			if (file == null) {
				throw new NullPointerException("file was null");
			}
			
			this.script = script;
			this.dir = dir;
			this.file = file;
		}
		
		@Override
		public Object exec(Context cx, Scriptable scriptable) {
			return this.script.exec(cx, scriptable);
		}

		@Override
		public String getFile() {
			return this.file;
		}

		@Override
		public String getAbsoluteDir() {
			return this.dir;
		}
		
	}
}

package com.davidehrmann.nodejava.scriptloader;

/**
 * A resolving ScriptLoader adds the notion of scripts coming from a file and a real path.
 */
public interface ResolvingScriptLoader<S> {
    ResolvedScript<S> loadScript(String relativePath, String pwd);

    interface ResolvedScript<S> {
        String getFile();

        String getAbsoluteDir();

        S getScript();
    }

    class DefaultResolvedScript<S> implements ResolvedScript<S> {

        protected final S script;
        protected final String file;
        protected final String dir;

        public DefaultResolvedScript(S script, String dir, String file) {
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

        /*
        public Object exec(Context cx, Scriptable scriptable) {
            return this.script.exec(cx, scriptable);
        }
        */

        public S getScript() {
            return this.script;
        }

        public String getFile() {
            return this.file;
        }

        public String getAbsoluteDir() {
            return this.dir;
        }
    }
}

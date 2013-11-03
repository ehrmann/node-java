package jnode.scriptloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import jnode.util.PaddingReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

public abstract class InputStreamScriptLoader implements ScriptLoader {
	
	protected static final char[] MATCH_PREFIX = "#!".toCharArray();
	protected static final char[] HEADER = "(function() { // ".toCharArray();
	protected static final char[] TRAILER = "\n})();".toCharArray();
	
	@Override
	public Script loadScript(String absolutePath) {
		try {
		InputStream in = this.getInputStream(absolutePath);
			try {
				InputStream bufferedIn = new BufferedInputStream(in);
				try {
					Reader reader = new InputStreamReader(in, "UTF-8");
					try {
						// TODO: this isn't an elegant place for shebang-stripping code
						PaddingReader paddingReader = new PaddingReader(reader, HEADER, TRAILER, MATCH_PREFIX);
						try {
							// cx seems to only be used for the compiler's env
							Context cx = Context.enter();
							return cx.compileReader(paddingReader, absolutePath, 1, null);
						} finally {
							paddingReader.close();
						}
					} finally {
						reader.close();
					}
				} finally {
					bufferedIn.close();
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			return null;
		}
	}
		
	protected abstract InputStream getInputStream(String absolutePath) throws IOException;

}

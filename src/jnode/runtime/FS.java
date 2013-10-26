package jnode.runtime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class FS {

	protected final Context cx;
	protected final Scriptable scope;
	
	public FS(Context cx, Scriptable scope) {
		if (cx == null) {
			throw new NullPointerException("context was null");
		}
		if (scope == null) {
			throw new NullPointerException("scope was null");
		}
		
		this.cx = cx;
		this.scope = scope;
	}
	
	// TODO: make this asynchronous
	public void readFile(String file, String encoding, Function callback) {
		Object[] args = { Context.getUndefinedValue(), Context.getUndefinedValue() };
		
		try {
			StringBuilder sb = new StringBuilder(2048);
			InputStream in = new FileInputStream(file);
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
				try {
					int r;
					while ((r = reader.read()) >= 0) {
						sb.append((char)r);
					}
				} finally {
					in.close();
				}
			} finally {
				in.close();
			}

			args[1] = Context.javaToJS(sb.toString(), scope);
		} catch (IOException e) {
			args[0] = Context.javaToJS(e, scope);
		}
		
		callback.call(cx, scope, callback, args);
	}
	
}

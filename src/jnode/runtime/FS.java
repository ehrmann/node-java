package jnode.runtime;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSFunction;

public class FS extends NativeObject {

	private static final long serialVersionUID = 3472871496662624798L;
	
	protected final Context cx;
	protected final Scriptable scope;
	
	public FS(Context cx, Scriptable scope) {
		super();
		
		if (cx == null) {
			throw new NullPointerException("context was null");
		}
		if (scope == null) {
			throw new NullPointerException("scope was null");
		}
		
		this.cx = cx;
		this.scope = scope;
		
		super.defaultPut("existsSync", new ExistsSync());
		super.defaultPut("readFile", new ReadFile());
		super.defaultPut("writeFileSync", new WriteFileSync());
		super.defaultPut("mkdirSync", new MkdirSync());
	}

	@Override
	public Object getDefaultValue(Class<?> typeHint) {
		return "fs";
	}
	
	protected abstract class FSFunction extends BaseFunction {
		private static final long serialVersionUID = 2936279843075497840L;

		public FSFunction() {
			super(FS.this.scope, FS.this.scope);
		}
	}
	
	protected class ExistsSync extends FSFunction {
		private static final long serialVersionUID = -2964017003217947062L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return FS.this.existsSync(args[0].toString());
		}
	}

	protected class ReadFile extends FSFunction {
		private static final long serialVersionUID = 6317513946924334392L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			FS.this.readFile(args[0].toString(), args[1].toString(), (Function)args[2]);
			return Context.getUndefinedValue();
		}
	}
	
	protected class WriteFileSync extends FSFunction {
		private static final long serialVersionUID = 8797102996639482881L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			FS.this.writeFileSync(args[0].toString(), args[1].toString(), args[2].toString());
			return Context.getUndefinedValue();
		}
	}

	protected class MkdirSync extends FSFunction {
		private static final long serialVersionUID = 200438842358703423L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length == 1) {
				FS.this.mkdirSync(args[0].toString());
			} else {
				FS.this.mkdirSync(args[0].toString(), ((Number)args[1]).intValue());
			}
			return Context.getUndefinedValue();
		}
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
	
	public void writeFileSync(String filename, String text, String encoding) {
		try {
			OutputStream out = new FileOutputStream(filename);
			try {
				OutputStream out2 = new BufferedOutputStream(out);
				try {
					Writer writer = new OutputStreamWriter(out2, encoding);
					try {
						writer.write(text);
					} finally {
						writer.close();
					}
				} finally {
					out2.close();
				}
			} finally {
				out.close();
			}
		} catch (IOException e) {
			throw new JavaScriptException(e, "writeFileSync", 1);
		}
	}
	
	
	@JSFunction
	public boolean existsSync(String file) {
		return (new File(file)).exists();
	}
	
	public void mkdirSync(String path) {
		mkdirSync(path, 777);
	}
	
	public void mkdirSync(String path, int mode) {
		File dir = new File(path);
		
		if (!dir.mkdir()) {
			throw new JavaScriptException("Failed to create directory: " + path, "mkdirSync", 1);
		}
		
		int owner = (mode / 100) % 10;
		int everyone = (mode / 1) % 10;
		
		dir.setExecutable((owner & 0x1) != 0, true);
		dir.setWritable((owner & 0x2) != 0, true);
		dir.setReadable((owner & 0x4) != 0, true);
		
		dir.setExecutable((everyone & 0x1) != 0, false);
		dir.setWritable((everyone & 0x2) != 0, false);
		dir.setReadable((everyone & 0x4) != 0, false);
	}

}

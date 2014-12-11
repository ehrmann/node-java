package com.davidehrmann.nodejava.runtime;

import com.davidehrmann.nodejava.script.AdapterFactory;

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
import java.util.function.Function;

public class FS {

    // TODO: make this asynchronous
    @NodeFunction
    public void readFile(String file, String encoding, Function<Object[], Object> callback) {
        Object[] args = { AdapterFactory.JAVA_UNDEFINED, AdapterFactory.JAVA_UNDEFINED };

        try {
            StringBuilder sb = new StringBuilder(2048);
            try (InputStream in = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding))) {
                int r;
                while ((r = reader.read()) >= 0) {
                    sb.append((char) r);
                }
            }

            args[1] = sb.toString();
        } catch (IOException e) {
            // TODO: richer exceptions
            args[0] = e.toString();
        }

        callback.apply(args);
    }

    @NodeFunction
    public void writeFileSync(String filename, String text, String encoding) {
        try (OutputStream out = new FileOutputStream(filename);
             OutputStream out2 = new BufferedOutputStream(out);
             Writer writer = new OutputStreamWriter(out2, encoding)) {
            writer.write(text);
        } catch (IOException e) {
            // TODO:
            throw new RuntimeException();
            //throw new JavaScriptException(e, "writeFileSync", 1);
        }
    }

    @NodeFunction
    public boolean existsSync(String file) {
        return (new File(file)).exists();
    }

    @NodeFunction
    public void mkdirSync(String path) {
        mkdirSync(path, 777);
    }

    @NodeFunction
    public void mkdirSync(String path, int mode) {
        File dir = new File(path);

        if (!dir.mkdir()) {
            // TODO:
            // throw new JavaScriptException("Failed to create directory: " + path, "mkdirSync", 1);
            throw new RuntimeException();
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

    /*
    protected class ExistsSync extends AbstractRuntimeFunction {
        private static final long serialVersionUID = -2964017003217947062L;

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            return FS.this.existsSync(args[0].toString());
        }
    }

    protected class ReadFile extends AbstractRuntimeFunction {
        private static final long serialVersionUID = 6317513946924334392L;

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            FS.this.readFile(args[0].toString(), args[1].toString(), (Function) args[2]);
            return Context.getUndefinedValue();
        }
    }

    protected class WriteFileSync extends AbstractRuntimeFunction {
        private static final long serialVersionUID = 8797102996639482881L;

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            FS.this.writeFileSync(args[0].toString(), args[1].toString(), args[2].toString());
            return Context.getUndefinedValue();
        }
    }

    protected class MkdirSync extends AbstractRuntimeFunction {
        private static final long serialVersionUID = 200438842358703423L;

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            if (args.length == 1) {
                FS.this.mkdirSync(args[0].toString());
            } else {
                FS.this.mkdirSync(args[0].toString(), ((Number) args[1]).intValue());
            }
            return Context.getUndefinedValue();
        }
    }
    */

}

package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.PackageStore;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.packagemanager.filestore.sqlite.SqlitePackageStore;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.script.ScriptRunner;
import com.davidehrmann.nodejava.script.rhino.RhinoAdapterFactory;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptCompiler;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptRunner;
import com.davidehrmann.nodejava.scriptloader.ChainedScriptLoader;
import com.davidehrmann.nodejava.scriptloader.PackageStoreScriptLoader;
import com.davidehrmann.nodejava.scriptloader.ScriptLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.sqlite.SQLiteDataSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class JNodeRunnerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testExpress() throws Exception {
        File packageStoreFile = this.folder.newFile();

        // TODO: replace with commons file copy
        try (OutputStream out = new FileOutputStream(packageStoreFile)) {
            try (InputStream in = this.getClass().getResourceAsStream("express.sqlite")) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0 ,read);
                }
            }
        }

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + packageStoreFile.getCanonicalPath());

        PackageStore packageStore = new SqlitePackageStore(dataSource, 1);

        try (InputStream in = packageStore.openFile("accepts", Version.fromString("1.2.6"), "index.js")) {
            int r;
            while ((r = in.read()) >= 0) {
                System.out.print((char)r);
            }
        }

        ScriptLoader mainScriptloader = new ScriptLoader() {
            @Override
            public InputStream loadScript(String canonicalPath) throws IOException {
                if ("/main.js".equals(canonicalPath)) {
                    String source = "" +
                            "var express = require('express');\n" +
                            "var app = express();\n" +
                            "app.get('/', function (req, res) {\n" +
                            "    res.send('Hello World!');\n" +
                            "});\n" +
                            "var server = app.listen(3000, function () {\n" +
                            "    var host = server.address().address;\n" +
                            "    var port = server.address().port;\n" +
                            "});\n" +
                            "";
                    return new ByteArrayInputStream(source.getBytes("UTF-8"));
                } else {
                    return null;
                }
            }
        };

        ScriptLoader depScriptLoader = new PackageStoreScriptLoader(packageStore, "express", VersionSpec.fromString("4.x"));

        ScriptLoader scriptLoader = new ChainedScriptLoader(Arrays.asList(mainScriptloader, depScriptLoader));
        ScriptCompiler<Script> scriptCompiler = new RhinoScriptCompiler();
        ScriptRunner<Object, Script> scriptRunner = new RhinoScriptRunner();
        RhinoAdapterFactory adapterFactory = new RhinoAdapterFactory(Context.getCurrentContext());
        JNodeRunner runner = new JNodeRunner(scriptLoader, scriptCompiler, scriptRunner, adapterFactory, "main.js", new String[] { });

        Integer code = runner.call();
    }

}

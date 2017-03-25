package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.*;
import com.davidehrmann.nodejava.packagemanager.filestore.sqlite.SqlitePackageStore;
import com.davidehrmann.nodejava.runtime.Console;
import com.davidehrmann.nodejava.runtime.*;
import com.davidehrmann.nodejava.script.AdapterFactory;
import com.davidehrmann.nodejava.script.CompilationException;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.script.rhino.RhinoAdapterFactory;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptCompiler;
import com.davidehrmann.nodejava.script.rhino.RhinoScriptRunner;
import com.davidehrmann.nodejava.scriptloader.*;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.sqlite.SQLiteDataSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class SqlitePackageManagerTest {

    protected static PackageStore packageStore;

    @BeforeClass
    public static void setUpPackageStore() throws IOException {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        File temp = File.createTempFile("pkgdb.sqlite", ".tmp");
        FileUtils.copyInputStreamToFile(SqlitePackageManagerTest.class.getResourceAsStream("pkgdb.sqlite"), temp);

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + temp.getCanonicalPath());
        packageStore = new SqlitePackageStore(dataSource, 1);
    }

    private ByteArrayOutputStream consoleOut = new ByteArrayOutputStream();

    protected final Module<Object, Script> module;
    protected final AdapterFactory adapterFactory;
    protected final Context context;
    protected final Map<String, Object> runtime;

    public SqlitePackageManagerTest(String packageName, VersionSpec versionSpec, String filename) throws PackageManagerException, NoMatchingPackageException, FileStoreException, IOException, CompilationException {
        ScriptLoader packageStoreScriptLoader = new PackageStoreScriptLoader(packageStore, packageName, versionSpec);
        ScriptLoader classpathScriptLoader = new ClasspathScriptLoader(this.getClass().getClassLoader());
        ScriptLoader scriptLoader = new ChainedScriptLoader(Arrays.asList(classpathScriptLoader, packageStoreScriptLoader));

        ScriptCompiler<Script> compiler = new RhinoScriptCompiler();
        ResolvingScriptLoader<Script> resolvingScriptLoader = new MemoizedResolvingScriptLoader<>(scriptLoader, new NodePathResolutionStrategy(), compiler);

        Console console = (new Console() {
            private Console setOut() {
                try {
                    out = new PrintStream(out, true, "US-ASCII");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return this;
            }
        }).setOut();

        this.runtime = ImmutableMap.<String, Object>builder()
                .put("console", console)
                .put("path", new Path())
                .put("fs", new FS())
                .put("url", new Url())
                .put("os", new Os())
                .build();

        this.module = new Module<>(runtime, Util.getPathOfClass(this), new RhinoScriptRunner(), resolvingScriptLoader, filename);
        this.context = Context.enter();
        this.adapterFactory = new RhinoAdapterFactory(context);
    }

    @After
    public void tearDown() {
        Context.exit();
    }

    protected String getConsoleOutString() {
        return new String(consoleOut.toByteArray(), StandardCharsets.US_ASCII);
    }

}

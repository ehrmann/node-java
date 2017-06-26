package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.packagemanager.filestore.sqlite.SqlitePackageStore;
import com.davidehrmann.nodejava.scriptloader.PackageStoreScriptLoader;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;

public class PackageTest {

    private static SqlitePackageStore packageStore;

    @BeforeClass
    public static void setUpPackageStore() throws IOException {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        File temp = File.createTempFile("pkgdb.sqlite", ".tmp");
        FileUtils.copyInputStreamToFile(SqlitePackageManagerTest.class.getResourceAsStream("pkgdb.sqlite"), temp);

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + temp.getCanonicalPath());
        packageStore = new SqlitePackageStore(dataSource, 1);
    }

    @Test
    public void testLess() throws Exception {
        // TODO: load optional and dev deps (if present)
        PackageStoreScriptLoader scriptLoader = new PackageStoreScriptLoader(
                packageStore,
                "less",
                VersionSpec.fromString("2.x"));

        // Should PackageStoreSCriptLoader have bin logic?

        // scripts.test
        // pretest, test, posttest: Run by the npm test command.

        // How are bin packages defined in package.json handled?

        // scrips vs bin

        // test/grunt test
    }
}

package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.script.CompilationException;
import org.junit.Test;

import java.io.IOException;

public class ExpressTest extends SqlitePackageManagerTest {
    public ExpressTest() throws PackageManagerException, NoMatchingPackageException, FileStoreException, IOException, CompilationException {
        super("express", VersionSpec.fromString("4.x"), "/express_test.js");
    }

    @Test
    public void testExpressApp() throws IOException, PackageManagerException, NoMatchingPackageException, FileStoreException {
        module.run();
    }
}

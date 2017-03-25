package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.*;
import com.davidehrmann.nodejava.packagemanager.filestore.sqlite.SqlitePackageStore;
import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmCouchRepository;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.net.URL;

public class PkgdbUtil {

    /**
     * This is a utility class for installing new packages in the test database
     */
    public static void main(String[] args) throws Exception {
        String packageName = null;
        VersionSpec versionSpec = null;

        if (args.length == 1) {
            String[] packageSpec = args[0].split("@");
            if (packageSpec.length == 2) {
                packageName = packageSpec[0];
                try {
                    versionSpec = VersionSpec.fromString(packageSpec[1]);
                } catch (IllegalArgumentException e) {
                    System.err.printf("Unable to parse versionSpec %s: %s%n", packageSpec[1], e.getMessage());
                }
            } else if (packageSpec.length == 1) {
                versionSpec = VersionSpec.LATEST;
            }
        }

        if (packageName == null || versionSpec == null) {
            System.err.printf("Usage: java %s package@versionSpec%n", PkgdbUtil.class.getName());
            System.exit(1);
        }

        URL pkgdbUrl = PkgdbUtil.class.getResource("pkgdb.sqlite");
        if (pkgdbUrl == null) {
            System.err.printf("Unable to locate pkgdb.sqlite on the classpath%n");
            System.exit(1);
        } else if (!"file".equals(pkgdbUrl.getProtocol())) {
            System.err.printf("%s on the classpath isn't a file%n", pkgdbUrl);
            System.exit(1);
        }

        File file = new File(pkgdbUrl.toURI());
        if (!file.exists()) {
            System.err.printf("%s doesn't exist%n", file);
            System.exit(1);
        } else if (!file.canWrite()) {
            System.err.printf("%s isn't writable%n", file);
            System.exit(1);
        }

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + file);

        PackageStore packageStore = new SqlitePackageStore(dataSource, 1);
        Repository repository = new NpmCouchRepository("https://skimdb.npmjs.com/registry/");

        PackageManager packageManager = new SimplePackageManager(packageStore);
        packageManager.addRepository("iris-couch", repository, 0);

        packageManager.installPackageAndDependencies(packageName, versionSpec, true, true);
    }
}

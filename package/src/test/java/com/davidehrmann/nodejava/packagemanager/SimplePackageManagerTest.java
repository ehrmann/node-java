package com.davidehrmann.nodejava.packagemanager;

import com.davidehrmann.nodejava.DependencyUtil;
import com.davidehrmann.nodejava.packagemanager.filestore.sqlite.SqlitePackageStore;
import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmCouchRepository;
import com.google.common.base.Joiner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SimplePackageManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testRealInstall() throws IOException, SQLException, NoMatchingVersionException, NoMatchingPackageException, RepositoryException, FileStoreException, PackageManagerException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        String file = folder.newFile().getCanonicalPath();
        dataSource.setUrl("jdbc:sqlite:" + file);
        int repositoryId;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("" +
                                "CREATE TABLE repositories ( " +
                                "  id INTEGER PRIMARY KEY NOT NULL " +
                                ")"
                );

                statement.executeUpdate("" +
                                "CREATE TABLE packages ( " +
                                "  id INTEGER PRIMARY KEY NOT NULL, " +
                                "  repository_id REFERENCES repositories(id) NOT NULL, " +
                                "  name TEXT NOT NULL, " +
                                "  major_ver INTEGER NOT NULL, " +
                                "  minor_ver INTEGER NOT NULL, " +
                                "  patch_ver INTEGER NOT NULL, " +
                                "  prerelease TEXT DEFAULT NULL, " +
                                "  manually_installed BOOLEAN NOT NULL " +
                                ")"
                );

                statement.executeUpdate("" +
                                "CREATE INDEX packages_name_index " +
                                "ON packages ( " +
                                "  name, " +
                                "  major_ver, " +
                                "  minor_ver, " +
                                "  patch_ver, " +
                                "  prerelease " +
                                ")"
                );

                statement.executeUpdate("" +
                                "CREATE TABLE packaged_files ( " +
                                "  package_id REFERENCES packages(id) ON DELETE CASCADE, " +
                                "  file_path TEXT, " +
                                "  file_sha1 TEXT COLLATE NOCASE, " +
                                "  PRIMARY KEY (package_id, file_path) " +
                                ")"
                );

                statement.executeUpdate("" +
                                "CREATE TABLE files ( " +
                                "  sha1 TEXT COLLATE NOCASE PRIMARY KEY NOT NULL, " +
                                "  encoding TEXT, " +
                                "  data BLOB NOT NULL " +
                                ")"
                );
            }

            try (PreparedStatement sss = conn.prepareStatement("INSERT INTO repositories DEFAULT VALUES")) {
                sss.execute();
            }

            try (PreparedStatement qqq = conn.prepareStatement("SELECT id FROM repositories LIMIT 1");
                 ResultSet rs = qqq.executeQuery()
            ) {
                rs.next();
                repositoryId = rs.getInt("id");
            }
        }

        PackageStore packageStore = new SqlitePackageStore(dataSource, repositoryId);
        Repository repository = new NpmCouchRepository("https://skimdb.npmjs.com/registry/");

        PackageManager packageManager = new SimplePackageManager(packageStore);
        packageManager.addRepository("iris-couch", repository, 0);

        packageManager.installPackageAndDependencies("express", VersionSpec.fromString("4.x"), true, true);
        packageManager.installPackageAndDependencies("express", VersionSpec.fromString("2.x"), true, true);
        System.err.println("file = " + file);
    }

}

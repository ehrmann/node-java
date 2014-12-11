package com.davidehrmann.nodejava.packagemanager.filestore.sqlite;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileAlreadyExistsException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class SqlitePackageStoreTest {

    private Version[] versions = new Version[] {
            Version.fromString("1.2.3"),
            Version.fromString("1.2.4"),
            Version.fromString("1.2.4-beta1"),
    };

    private String[] packageNames = new String[] {
        "less",
        "mkdirp",
    };

    private DataSource dataSource = null;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setupDb() throws SQLException, IOException {
        SQLiteDataSource dataSource = new SQLiteDataSource();

        dataSource.setUrl("jdbc:sqlite:" +  folder.newFile().getCanonicalPath());


        //dataSource.setUrl("jdbc:sqlite::memory:");

        this.dataSource = dataSource;

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
                                "  sha1 TEXT COLLATE NOCASE PRIMARY KEY NOT NULL, "+
                                "  encoding TEXT, "+
                                "  data BLOB NOT NULL " +
                                ")"
                );
            }

            try (PreparedStatement sss = conn.prepareStatement("INSERT INTO repositories DEFAULT VALUES")) {
                sss.execute();
            }

            long repositoryId;
            try (PreparedStatement qqq = conn.prepareStatement("SELECT id FROM repositories LIMIT 1");
                 ResultSet rs = qqq.executeQuery()
            ) {
                rs.next();
                repositoryId = rs.getLong("id");
            }

            try (PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO packages (repository_id, manually_installed, name, major_ver, minor_ver, patch_ver, prerelease) VALUES(?, 1, ?, ?, ?, ?, ?)")) {
                for (String packageName : this.packageNames) {
                    for (Version version : this.versions) {
                        preparedStatement.setLong(1, repositoryId);
                        preparedStatement.setString(2, packageName);
                        preparedStatement.setInt(3, version.getMajor());
                        preparedStatement.setInt(4, version.getMinor());
                        preparedStatement.setInt(5, version.getPatch());
                        preparedStatement.setString(6, version.getPrerelease());

                        preparedStatement.addBatch();
                    }
                }

                preparedStatement.executeBatch();
            }
        }
    }

    @Test
    public void testCreateFile() throws IOException, FileStoreException {
        SqlitePackageStore fileStore = new SqlitePackageStore(this.dataSource, 1);
        String[] paths = new String[] {
                "/resolveDependencies",
                "/bar",
                "/baz/qux",
                "/baz/quux",
                "/.manifest/${version}",
        };

        for (String packageName : this.packageNames) {
            for (Version version : this.versions) {
                for (String path: paths) {
                    path = path.replace("${version}", version.toString());
                    String fileContents =  String.format("# %s-%s%s", packageName, version, path);
                    fileStore.createFile(packageName, version, path, new ByteArrayInputStream(fileContents.getBytes("ASCII")));
                }
            }
        }

        for (String packageName : this.packageNames) {
            for (Version version : this.versions) {
                for (String path: paths) {
                    path = path.replace("${version}", version.toString());
                    String expectedFileContents =  String.format("# %s-%s%s", packageName, version, path);
                    StringBuilder actualFileContents = new StringBuilder(expectedFileContents.length());

                    try (InputStream in = fileStore.openFile(packageName, version, path);
                         Reader reader = new InputStreamReader(in, "ASCII")
                    ) {
                        int c;
                        while ((c = reader.read()) >= 0) {
                            actualFileContents.append((char)c);
                        }
                    }

                    assertEquals(expectedFileContents, actualFileContents.toString());
                }
            }
        }
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void testFileAlreadyExists() throws IOException, FileStoreException {
        SqlitePackageStore fileStore = new SqlitePackageStore(this.dataSource, 1);

        byte[] fileContents =  "Lorem ipsum".getBytes("ASCII");
        try {
            fileStore.createFile("less", Version.fromString("1.2.3"), "/doc", new ByteArrayInputStream(fileContents));
        } catch (FileStoreException|IOException e) {
            Assert.fail();
            e.printStackTrace();
        }

        fileStore.createFile("less", Version.fromString("1.2.3"), "/doc", new ByteArrayInputStream(fileContents));
    }

    @Test(expected = FileNotFoundException.class)
    public void testMissingFile() throws IOException, FileStoreException {
        SqlitePackageStore fileStore = new SqlitePackageStore(this.dataSource, 1);
        fileStore.openFile("less", Version.fromString("1.2.3"), "/does-not-exist").close();
    }


}

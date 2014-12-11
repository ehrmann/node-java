package com.davidehrmann.nodejava.packagemanager.filestore.sqlite;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.Package;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.PackageStore;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SqlitePackageStore implements PackageStore {

    private static final String ACCESS_ERROR_STRING = "Error accessing file store";

    private final DataSource dataSource;
    private final int repositoryId;

    public SqlitePackageStore(DataSource dataSource, int repositoryId) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.repositoryId = repositoryId;
    }

    // TODO: canonicalize path
    @Override
    public void createFile(String packageName, Version version, String path, InputStream in) throws IOException, FileStoreException {
        byte[] compressed;
        byte[] hash;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gz = new GZIPOutputStream(out);
        ) {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-1 not supported", e);
            }

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                gz.write(buffer, 0, read);
                sha1.update(buffer, 0, read);
            }

            gz.close();
            compressed = out.toByteArray();
            hash = sha1.digest();
        }

        String sha1Str = DatatypeConverter.printHexBinary(hash);

        try (Connection conn = this.dataSource.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                long packageId;

                try (PreparedStatement statement = conn.prepareStatement("SELECT id FROM packages WHERE name = ? AND major_ver = ? AND minor_ver = ? AND patch_ver = ? AND (prerelease = ? OR COALESCE(prerelease, ?) IS NULL)")) {
                    statement.setString(1, packageName);
                    statement.setInt(2, version.getMajor());
                    statement.setInt(3, version.getMinor());
                    statement.setInt(4, version.getPatch());
                    statement.setString(5, version.getPrerelease());
                    statement.setString(6, version.getPrerelease());

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new FileStoreException("package " + packageName + "-" + version + " doesn't exist");
                        }

                        packageId = rs.getLong("id");
                    }
                }

                // Ignore duplicate keys because the file's already there
                try (PreparedStatement statement = conn.prepareStatement("INSERT OR IGNORE INTO files (sha1, encoding, data) VALUES (?, ?, ?)")) {
                    statement.setString(1, sha1Str);
                    statement.setString(2, "gzip");
                    statement.setBytes(3, compressed);

                    statement.execute();
                }

                try (PreparedStatement statement2 = conn.prepareStatement("INSERT INTO packaged_files (package_id, file_path, file_sha1) VALUES(?, ?, ?)")) {
                    statement2.setLong(1, packageId);
                    statement2.setString(2, path);
                    statement2.setString(3, sha1Str);

                    statement2.execute();
                } catch (SQLException e) {
                    throw new FileAlreadyExistsException(getPackagedFileString(packageName, version, path));
                }

                conn.commit();
            } catch (SQLException e) {
                // TODO:
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new FileStoreException(ACCESS_ERROR_STRING, e);
        }
    }

    @Override
    public InputStream openFile(String packageName, Version version, String path) throws IOException, FileStoreException {
        String encoding;
        byte[] data;

        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("" +
                    "SELECT files.encoding, files.data " +
                    "FROM packages " +
                    "JOIN packaged_files ON packages.id = packaged_files.package_id " +
                    "JOIN files ON files.sha1 = packaged_files.file_sha1 " +
                    "WHERE name = ? AND major_ver = ? AND minor_ver = ? AND patch_ver = ? AND (prerelease = ? OR COALESCE(prerelease, ?) is NULL) AND file_path = ?")
            ) {
                statement.setString(1, packageName);
                statement.setInt(2, version.getMajor());
                statement.setInt(3, version.getMinor());
                statement.setInt(4, version.getPatch());
                statement.setString(5, version.getPrerelease());
                statement.setString(6, version.getPrerelease());
                statement.setString(7, path);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        encoding = rs.getString("encoding");
                        data = rs.getBytes("data");
                    } else {
                        throw new FileNotFoundException(getPackagedFileString(packageName, version, path) + " wasn't found");
                    }
                }
            } catch (SQLException e) {
                throw new IOException(e);
            }

            if (encoding == null || encoding.isEmpty()) {
                return new ByteArrayInputStream(data);
            } else if ("gzip".equals(encoding)) {
                return new GZIPInputStream(new ByteArrayInputStream(data));
            } else {
                throw new FileStoreException("Unsupported encoding '" + encoding + "'");
            }
        } catch (SQLException e) {
            throw new FileStoreException(ACCESS_ERROR_STRING, e);
        }
    }

    @Override
    public Set<Package> listPackages() throws PackageManagerException, IOException, FileStoreException {
        Set<Package> packages = new HashSet<>();
        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("" +
                            "SELECT name, major_ver, minor_ver, patch_ver, prerelease FROM packages WHERE repository_id = ?"
            )) {
                statement.setInt(1, this.repositoryId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");

                        int majorVer = resultSet.getInt("major_ver");
                        int minorVer = resultSet.getInt("minor_ver");
                        int patchVer = resultSet.getInt("patch_ver");
                        String prerelease = resultSet.getString("prerelease");

                        Version version = new Version(majorVer, minorVer, patchVer, prerelease, null);

                        packages.add(new SqlitePackage(name, version, this));
                    }
                }
            } catch (SQLException e) {

            }
        } catch (SQLException e) {
            throw new FileStoreException(ACCESS_ERROR_STRING, e);
        }

        return packages;
    }

    @Override
    public NavigableMap<Version, Package> getPackages(String packageName, VersionSpec versionSpec) throws IOException, PackageManagerException, FileStoreException {
        NavigableMap<Version, Package> packages = new TreeMap<>();
        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("" +
                            "SELECT major_ver, minor_ver, patch_ver, prerelease FROM packages WHERE repository_id = ? AND name = ?"
            )) {
                statement.setInt(1, this.repositoryId);
                statement.setString(2, packageName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int majorVer = resultSet.getInt("major_ver");
                        int minorVer = resultSet.getInt("minor_ver");
                        int patchVer = resultSet.getInt("patch_ver");
                        String prerelease = resultSet.getString("prerelease");

                        Version version = new Version(majorVer, minorVer, patchVer, prerelease, null);
                        if (versionSpec.matches(version)) {
                            packages.put(version, new SqlitePackage(packageName, version, this));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            throw new FileStoreException(ACCESS_ERROR_STRING, e);
        }

        return packages;
    }

    @Override
    public void deletePackage(String packageName, Version version) throws FileStoreException {
        try (Connection conn = this.dataSource.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try (PreparedStatement statement = conn.prepareStatement("" +
                            "DELETE FROM packages " +
                            "WHERE repository_id = ? AND name = ? AND major_ver = ? AND minor_ver = ? AND patch_ver = ? AND (prerelease = ? OR COALESCE(prerelease, ?)"

            )) {
                conn.setAutoCommit(false);

                statement.setInt(1, this.repositoryId);
                statement.setString(2, packageName);
                statement.setInt(3, version.getMajor());
                statement.setInt(4, version.getMinor());
                statement.setInt(5, version.getPatch());
                statement.setString(6, version.getPrerelease());
                statement.setString(7, version.getPrerelease());

                int deleteCount = statement.executeUpdate();

                if (deleteCount == 1) {
                    conn.commit();
                } else if (deleteCount == 0) {
                    // todo
                    throw new RuntimeException("Package not found");
                } else if (deleteCount > 1) {
                    throw new FileStoreException("Corrupt file store; duplicate packages exist for " + packageName + "-" + version);
                }
            } catch (SQLException e) {
                throw new FileStoreException("Error deleting package", e);
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new FileStoreException(ACCESS_ERROR_STRING, e);
        }
    }

    @Override
    public void createPackage(String packageName, Version version, boolean manuallyInstalled) throws FileStoreException {
        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("" +
                            "INSERT INTO packages (" +
                            "  repository_id, " +
                            "  name, " +
                            "  major_ver, " +
                            "  minor_ver, " +
                            "  patch_ver, " +
                            "  prerelease, " +
                            "  manually_installed) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            )) {
                statement.setInt(1, this.repositoryId);
                statement.setString(2, packageName);
                statement.setInt(3, version.getMajor());
                statement.setInt(4, version.getMinor());
                statement.setInt(5, version.getPatch());
                statement.setString(6, version.getPrerelease());
                statement.setBoolean(7, manuallyInstalled);

                statement.execute();
            } catch (SQLException e) {

            }
        } catch (SQLException e) {
            throw new FileStoreException(ACCESS_ERROR_STRING, e);
        }
    }

    protected String getPackagedFileString(String packageName, Version version, String path) {
        return packageName + "-" + version + ":" + path;
    }
}

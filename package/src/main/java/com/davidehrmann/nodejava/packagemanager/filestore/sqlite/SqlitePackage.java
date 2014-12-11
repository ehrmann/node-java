package com.davidehrmann.nodejava.packagemanager.filestore.sqlite;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.Package;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmPackageDescriptor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class SqlitePackage implements Package {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String name;
    private final Version version;
    private final SqlitePackageStore packageStore;
    private final NpmPackageDescriptor packageDescriptor;

    SqlitePackage(String name, Version version, SqlitePackageStore packageStore) throws IOException, PackageManagerException, FileStoreException {
        this.name = Objects.requireNonNull(name);
        this.version = Objects.requireNonNull(version);
        this.packageStore = Objects.requireNonNull(packageStore);
        try (InputStream in = packageStore.openFile(name, version, "package.json")) {
            this.packageDescriptor = OBJECT_MAPPER.readValue(in, NpmPackageDescriptor.class);
        } catch (JsonMappingException | JsonParseException e) {
            throw new PackageManagerException("Package descriptor parse error", e);
        }

        if (!this.name.equals(this.packageDescriptor.getName())) {
            throw new PackageManagerException("Package file store/descriptor name mismatch");
        } else if (!this.version.equals(this.packageDescriptor.getVersion())) {
            throw new PackageManagerException("Package file store/descriptor version mismatch");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Version getVersion() {
        return this.version;
    }

    @Override
    public InputStream openFile(String path) throws FileStoreException, IOException {
        return this.packageStore.openFile(this.name, this.version, path);
    }

    @Override
    public Map<String, VersionSpec> getDependencies() {
        return this.packageDescriptor.getDependencies();
    }

}

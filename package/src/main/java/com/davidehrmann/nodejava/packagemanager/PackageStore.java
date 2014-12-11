package com.davidehrmann.nodejava.packagemanager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableMap;
import java.util.Set;

public interface PackageStore {
    void createFile(String packageName, Version version, String path, InputStream in) throws IOException, FileStoreException;

    InputStream openFile(String packageName, Version version, String path) throws FileNotFoundException, IOException, FileStoreException;

    Set<Package> listPackages() throws FileStoreException, PackageManagerException, IOException;

    NavigableMap<Version, Package> getPackages(String packageName, VersionSpec versionSpec) throws IOException, PackageManagerException, FileStoreException;

    void deletePackage(String packageName, Version version) throws FileStoreException;

    void createPackage(String packageName, Version version, boolean manuallyInstalled) throws FileStoreException;
}

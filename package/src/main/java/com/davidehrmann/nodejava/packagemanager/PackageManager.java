package com.davidehrmann.nodejava.packagemanager;

import java.io.IOException;

public interface PackageManager {
    void addRepository(String alias, Repository repository, int rank);

    Package getPackage(String name, VersionSpec versionSpec) throws PackageManagerException, IOException, FileStoreException;

    // TODO: How to handle init scripts
    // 1) install to real path, to ~/.jnode/bin, or use java -jar jnode run-package|run-package-file <pkg> <file path>
    void installPackage(String name, VersionSpec versionSpec) throws NoMatchingVersionException, IOException, NoMatchingPackageException, RepositoryException, FileStoreException;

    void installPackageAndDependencies(String name, VersionSpec versionSpec) throws NoMatchingVersionException, IOException, NoMatchingPackageException, RepositoryException, FileStoreException, PackageManagerException;

    void uninstallPackage(String name, VersionSpec versionSpec);

    void reinstallPackage(String name, VersionSpec versionSpec);

    // TODO: have this?
    void update();

    void upgrade(String packageName
    );

    /**
     * Upgrade all explicitly installed packages to their latest release versions.
     */
    void upgrade();

    /**
     * Upgrade all explicitly installed packages to the latest release version in the current release version.
     */
    void safeUpgrade();

    /**
     * Remove all unneeded implicitly installed packages.
     */
    void clean();
}

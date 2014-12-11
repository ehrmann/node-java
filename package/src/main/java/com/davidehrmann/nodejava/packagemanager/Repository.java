package com.davidehrmann.nodejava.packagemanager;

import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmRegistryEntry;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public interface Repository {
    Set<Object> getAvailablePackages();

    Map.Entry<Version, NpmRegistryEntry.VersionInfo> getMatchingEntry(String packageName, VersionSpec versionSpec) throws NoMatchingPackageException, NoMatchingVersionException, RepositoryException;

    NavigableMap<Version, NpmRegistryEntry.VersionInfo> getMatchingEntries(String packageName, VersionSpec versionSpec) throws RepositoryException;
}

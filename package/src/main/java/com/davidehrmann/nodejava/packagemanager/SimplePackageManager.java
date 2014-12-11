package com.davidehrmann.nodejava.packagemanager;

import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmRegistryEntry;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SimplePackageManager implements PackageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePackageManager.class);

    NavigableMap<Integer, Repository> repositoryMap = new TreeMap<>();
    Map<String, Integer> aliasMap = new HashMap<>();

    PackageStore packageStore;

    public SimplePackageManager(PackageStore packageStore) {
        this.packageStore = Objects.requireNonNull(packageStore);
    }

    @Override
    public void addRepository(String alias, Repository repository, int rank) {
        Objects.requireNonNull(repository);
        if (aliasMap.containsKey(alias)) {
            throw new IllegalArgumentException("Alias already used");
        }
        if (repositoryMap.containsKey(rank)) {
            throw new IllegalArgumentException("Rank already used");
        }

        repositoryMap.put(rank, repository);
        aliasMap.put(alias, rank);
    }

    @Override
    public Package getPackage(String name, VersionSpec versionSpec) throws PackageManagerException, IOException, FileStoreException {
        NavigableMap<Version, Package> storedPackages = this.packageStore.getPackages(name, versionSpec);
        if (!storedPackages.isEmpty()) {
            return storedPackages.lastEntry().getValue();
        }

        return null;
    }

    @Override
    public void installPackage(String name, VersionSpec versionSpec) throws NoMatchingVersionException, IOException, NoMatchingPackageException, RepositoryException, FileStoreException {
        this.installPackage(name, versionSpec, true);
    }

    @Override
    public void installPackageAndDependencies(String name, VersionSpec versionSpec) throws NoMatchingVersionException, IOException, NoMatchingPackageException, RepositoryException, FileStoreException, PackageManagerException {
        Map<String, Version> selectedPackages = new LinkedHashMap<>();
        if (this.resolveDependencies(name, versionSpec, selectedPackages)) {
            for (Map.Entry<String, Version> entry : selectedPackages.entrySet()) {
                // TODO: Version->VersionSpec is messy and probably broken for suffixed versions
                VersionSpec versionSpecToInstall = VersionSpec.fromString(entry.getValue().toString());
                String packageToInstall = entry.getKey();
                boolean manual = name.equals(packageToInstall);
                this.installPackage(packageToInstall, versionSpecToInstall, manual);
            }
        }
    }

    @Override
    public void uninstallPackage(String name, VersionSpec versionSpec) {

    }

    @Override
    public void reinstallPackage(String name, VersionSpec versionSpec) {

    }

    @Override
    public void update() {

    }

    @Override
    public void upgrade(String packageName) {

    }

    @Override
    public void upgrade() {

    }

    @Override
    public void safeUpgrade() {

    }

    @Override
    public void clean() {

    }

    protected void installPackage(String packageName, VersionSpec versionSpec, boolean manuallyInstalled) throws NoMatchingPackageException, NoMatchingVersionException, RepositoryException, FileStoreException, IOException {
        Set<Version> availableVersions = new HashSet<>();
        boolean packageAvailable = false;

        for (Repository repository : repositoryMap.values()) {
            try {
                Map.Entry<Version, NpmRegistryEntry.VersionInfo> repoEntry = repository.getMatchingEntry(packageName, versionSpec);
                packageAvailable = true;

                URL url = repoEntry.getValue().getDistInfo().getTarball();

                byte[] buffer = new byte[8192];

                File temp = File.createTempFile(SimplePackageManager.class.getSimpleName(), null);
                try {
                    try (
                            InputStream in = url.openStream();
                            OutputStream out = new FileOutputStream(temp)
                    ) {
                        int read;
                        while ((read = in.read(buffer)) >= 0) {
                            out.write(buffer, 0, read);
                        }
                    }

                    try (
                            InputStream in = new FileInputStream(temp);
                            InputStream in2 = url.getFile().matches(".*(?i)[.](?:tgz|gz)$") ? new GZIPInputStream(in) : in;
                            InputStream in3 = new BufferedInputStream(in2, 8192)
                    ) {
                        try (ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(in3)) {
                            packageStore.createPackage(packageName, repoEntry.getKey(), manuallyInstalled);

                            ArchiveEntry entry;
                            while ((entry = input.getNextEntry()) != null) {
                                if (entry.isDirectory()) {
                                    continue;
                                } else if (!input.canReadEntryData(entry)) {
                                    continue;
                                }

                                packageStore.createFile(packageName, repoEntry.getKey(), entry.getName().replaceFirst("^/?package/", ""), input);
                            }
                        } catch (ArchiveException e) {
                            throw new RepositoryException("Unable to read package from " + url, e);
                        }
                    }
                } finally {
                    temp.delete();
                }

                return;
            } catch (NoMatchingPackageException e) {
                // Ignore and try a different repository
            } catch (NoMatchingVersionException e) {
                Map<Version, NpmRegistryEntry.VersionInfo> entries = repository.getMatchingEntries(packageName, VersionSpec.fromString("*"));
                availableVersions.addAll(entries.keySet());
                packageAvailable = true;
            }
        }

        if (!packageAvailable) {
            throw new NoMatchingPackageException();
        } else {
            throw new NoMatchingVersionException(packageName, availableVersions, versionSpec);
        }
    }

    protected boolean resolveDependencies(String packageName, VersionSpec versionSpec, Map<String, Version> selectedPackages) throws RepositoryException {
        for (Repository repository : repositoryMap.values()) {
            // TODO: cache getMatchingEntries
            LOGGER.debug("Searching for package {} {} in {}", packageName, versionSpec, repository);
            Map<Version, NpmRegistryEntry.VersionInfo> candidatePackages = repository.getMatchingEntries(packageName, versionSpec);

            if (candidatePackages.isEmpty()) {
                LOGGER.debug("No matches found for {} {}", packageName, versionSpec);
            } else {
                LOGGER.debug("Candidates found for {}: {}", packageName, candidatePackages.keySet());
                }

            packageLoop:
            for (Map.Entry<Version, NpmRegistryEntry.VersionInfo> entry : candidatePackages.entrySet()) {
                selectedPackages.put(packageName, entry.getKey());
                LOGGER.debug("Resolving dependencies for {} {}", packageName, entry.getKey());

                for (Map.Entry<String, VersionSpec> dependencyEntry : entry.getValue().getDependencies().entrySet()) {
                    Version selectedVersion = selectedPackages.get(dependencyEntry.getKey());
                    if (selectedVersion != null) {
                        if (!dependencyEntry.getValue().matches(selectedVersion)) {
                            continue packageLoop;
                        }
                    } else {
                        if (!resolveDependencies(dependencyEntry.getKey(), dependencyEntry.getValue(), selectedPackages)) {
                            continue packageLoop;
                        }
                    }
                }

                return true;
            }

            selectedPackages.remove(packageName);
        }

        return false;
    }

    private final static class InstallQueueItem {
        private final String packageName;
        private final VersionSpec versionSpec;
        private final boolean manuallyInstalled;

        public InstallQueueItem(String packageName, VersionSpec versionSpec, boolean manuallyInstalled) {
            this.packageName = Objects.requireNonNull(packageName);
            this.versionSpec = Objects.requireNonNull(versionSpec);
            this.manuallyInstalled = manuallyInstalled;
        }
    }
}

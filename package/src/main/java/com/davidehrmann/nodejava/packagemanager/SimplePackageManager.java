package com.davidehrmann.nodejava.packagemanager;

import com.davidehrmann.nodejava.DependencyUtil;
import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmRegistryEntry;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
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
    public void installPackageAndDependencies(String name, VersionSpec versionSpec, boolean devDependencies, boolean optionalDependencies) throws NoMatchingVersionException, IOException, NoMatchingPackageException, RepositoryException, FileStoreException, PackageManagerException {
        Map<String, Version> selectedPackages = DependencyUtil.resolveDependencies(
                DependencyUtil.memoizedPackageResolver(this.packageResolver(devDependencies, optionalDependencies)),
                name,
                versionSpec
        );
        if (!selectedPackages.isEmpty()) {
            for (Map.Entry<String, Version> entry : selectedPackages.entrySet()) {
                // TODO: Version->VersionSpec is messy and probably broken for suffixed versions
                VersionSpec versionSpecToInstall = VersionSpec.fromString(entry.getValue().toString());
                Package pkg = null;
                try {
                    pkg = this.getPackage(entry.getKey(), versionSpecToInstall);
                } catch (FileNotFoundException e) {
                    LOGGER.warn("Corrupt package?", e);
                }

                if (pkg == null) {
                    String packageToInstall = entry.getKey();
                    boolean manual = name.equals(packageToInstall);
                    this.installPackage(packageToInstall, versionSpecToInstall, manual);
                }

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

        // FIXME: gracefully handle packages already installed, possibly updating manuallyInstalled

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

                                // TODO: investigate the right behavior: http://stackoverflow.com/questions/42894900/
                                packageStore.createFile(packageName, repoEntry.getKey(), entry.getName().replaceFirst("^/?[^/]+/", ""), input);
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

    private DependencyUtil.PackageResolver<RepositoryException> packageResolver(boolean devDependencies, boolean optionalDependencies) {
        return (packageName, versionSpec) -> {
            Map<Version, Map<String, VersionSpec>> result = Collections.emptyMap();

            for (Repository repository : repositoryMap.values()) {
                result = repository.getMatchingEntries(packageName, versionSpec).entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> versionInfoToVersionSpecs(e.getValue(), devDependencies, optionalDependencies),
                                (v, u) -> v,
                                LinkedHashMap::new
                        ));

                if (!result.isEmpty()) {
                    break;
                }
            }
            return result;
        };
    }

    private Map<String, VersionSpec> versionInfoToVersionSpecs(NpmRegistryEntry.VersionInfo versionInfo, boolean devDependencies, boolean optionalDependencies) {
        Map<String, VersionSpec> result = new LinkedHashMap<>();

        if (devDependencies && versionInfo.getDevDependencies() != null) {
            result.putAll(versionInfo.getDevDependencies());
        }
        if (optionalDependencies && versionInfo.getOptionalDependencies() != null) {
            result.putAll(versionInfo.getOptionalDependencies());
        }
        if (versionInfo.getDependencies() != null) {
            result.putAll(versionInfo.getDependencies());
        }

        return result;
    }
}

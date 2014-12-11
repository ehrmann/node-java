package com.davidehrmann.nodejava.scriptloader;

import com.davidehrmann.nodejava.DependencyUtil;
import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.Package;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.PackageStore;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.packagemanager.repository.npm.NpmRegistryEntry;
import com.davidehrmann.nodejava.util.Path;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PackageStoreScriptLoader implements ScriptLoader {

    protected static final Pattern MODULES_PATTERN = Pattern.compile("/" + Pattern.quote(Path.MODULE_PATH_ELEMENT) + "/([^/]*)/?(.*)");

    protected final PackageStore packageStore;
    protected final Map<String, Package> packageMap;

    public PackageStoreScriptLoader(PackageStore packageStore, String packageName, VersionSpec versionSpec) throws PackageManagerException, IOException, FileStoreException, NoMatchingPackageException {
        this.packageStore = Objects.requireNonNull(packageStore);

        Map<String, Version> f = DependencyUtil.resolveDependencies(packageResolver(), packageName, versionSpec);

        if (f.isEmpty()) {
            throw new NoMatchingPackageException();
        }

        Map<String, Package> packageMap = new HashMap<>();
        for (Map.Entry<String, Version> entry : f.entrySet()) {
            NavigableMap<Version, Package> resolved = this.packageStore.getPackages(entry.getKey(), VersionSpec.fromString(entry.getValue().toString()));
            if (resolved.isEmpty()) {
                throw new IllegalStateException();
            }
            packageMap.put(entry.getKey(), resolved.firstEntry().getValue());
        }

        // TODO: Once Package gets a list method, memoize available files

        this.packageMap = Collections.unmodifiableMap(packageMap);
    }

    @Override
    public InputStream loadScript(String canonicalPath) throws IOException {
        Matcher matcher = MODULES_PATTERN.matcher(canonicalPath);
        if (!matcher.matches()) {
            throw new FileNotFoundException();
        }

        String packageName = matcher.group(1);
        String packageAbsolutePath = matcher.group(2);

        Package pkg = this.packageMap.get(packageName);
        if (pkg != null) {
            try {
                return pkg.openFile(packageAbsolutePath);
            } catch (FileStoreException e) {
                throw new IOException(e);
            }
        } else {
            throw new FileNotFoundException("Package " + packageName + " not in materialized PackageStore");
        }
    }

    private static class PackageSpec {
        public final String packageName;
        public final VersionSpec versionSpec;

        public PackageSpec(String packageName, VersionSpec versionSpec) {
            this.packageName = Objects.requireNonNull(packageName);
            this.versionSpec = Objects.requireNonNull(versionSpec);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PackageSpec that = (PackageSpec) o;
            return Objects.equals(this.packageName, that.packageName) && Objects.equals(this.versionSpec, that.versionSpec);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageName, versionSpec);
        }
    }

    private DependencyUtil.PackageResolver packageResolver() {
        return (packageName, versionSpec) -> {
            NavigableMap<Version, Package> packages = packageStore.getPackages(packageName, versionSpec);
            return packages.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getDependencies()))
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (v, u) -> v,
                                    () -> new TreeMap<>(packages.comparator())
                            )
                    );
        };
    }
}

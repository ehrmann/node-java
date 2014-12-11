package com.davidehrmann.nodejava.scriptloader;

import com.davidehrmann.nodejava.packagemanager.FileStoreException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.Package;
import com.davidehrmann.nodejava.packagemanager.PackageManagerException;
import com.davidehrmann.nodejava.packagemanager.PackageStore;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.util.Path;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PackageStoreScriptLoader implements ScriptLoader {

    protected static final Pattern MODULES_PATTERN = Pattern.compile("/" + Pattern.quote(Path.MODULE_PATH_ELEMENT) + "/([^/]*)/?(.*)");

    protected final PackageStore packageStore;
    protected final Map<String, Package> packageMap;

    public PackageStoreScriptLoader(PackageStore packageStore, String packageName, VersionSpec versionSpec) throws PackageManagerException, IOException, FileStoreException, NoMatchingPackageException {
        this.packageStore = Objects.requireNonNull(packageStore);

        // This approach optimistically chooses the latest available version of every required package
        Deque<PackageSpec> queue = new ArrayDeque<>();
        Map<String, Package> versionMap = new HashMap<>();

        queue.add(new PackageSpec(packageName, versionSpec));

        while (!queue.isEmpty()) {
            PackageSpec packageSpec = queue.removeFirst();

            NavigableMap<Version, Package> packages = this.packageStore.getPackages(packageSpec.packageName, packageSpec.versionSpec);

            if (!packages.isEmpty()) {
                Package newPackage = packages.firstEntry().getValue();
                Package currentPackage = versionMap.get(newPackage.getName());
                if (currentPackage == null || currentPackage.getVersion().compareTo(newPackage.getVersion()) < 0) {
                    versionMap.put(newPackage.getName(), newPackage);
                    for (Map.Entry<String, VersionSpec> entry : newPackage.getDependencies().entrySet()) {
                        queue.addLast(new PackageSpec(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }

        // This could have added packages that are no longer needed.  Remove them.
        Map<String, Package> packageMap = new HashMap<>();

        Deque<String> packageQueue = new ArrayDeque<>();
        packageQueue.addLast(packageName);

        while (!packageQueue.isEmpty()) {
            String currentPackage = packageQueue.removeFirst();

            Package c = versionMap.get(currentPackage);
            if (c == null) {
                throw new NoMatchingPackageException();
            }

            packageMap.put(c.getName(), c);

            for (String k : c.getDependencies().keySet()) {
                packageQueue.addLast(k);
            }
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
}

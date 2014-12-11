package com.davidehrmann.nodejava;

import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyUtil.class);

    public static <E extends Exception> Map<String, Version> resolveDependencies(
            PackageResolver<E> resolver,
            String packageName,
            VersionSpec versionSpec) throws E {
        Map<String, Version> selectedPackages = new LinkedHashMap<>();
        resolveDependencies(resolver, packageName, versionSpec, selectedPackages);
        return selectedPackages;
    }

    private static <E extends Exception> boolean resolveDependencies(
            PackageResolver<E> resolver,
            String packageName, VersionSpec versionSpec,
            Map<String, Version> selectedPackages) throws E {
        LOGGER.debug("Searching for package {} {}", packageName, versionSpec);
        Map<Version, Map<String, VersionSpec>> candidatePackages = resolver.getMatchingEntries(packageName, versionSpec);

        if (candidatePackages.isEmpty()) {
            LOGGER.debug("No matches found for {} {}", packageName, versionSpec);
        } else {
            LOGGER.debug("Candidates found for {}: {}", packageName, candidatePackages.keySet());
        }

        packageLoop:
        for (Map.Entry<Version, Map<String, VersionSpec>> entry : candidatePackages.entrySet()) {
            selectedPackages.put(packageName, entry.getKey());
            LOGGER.debug("Resolving dependencies for {} {}", packageName, entry.getKey());

            for (Map.Entry<String, VersionSpec> dependencyEntry : entry.getValue().entrySet()) {
                Version selectedVersion = selectedPackages.get(dependencyEntry.getKey());
                if (selectedVersion != null) {
                    if (!dependencyEntry.getValue().matches(selectedVersion)) {
                        continue packageLoop;
                    }
                } else {
                    if (!resolveDependencies(resolver, dependencyEntry.getKey(), dependencyEntry.getValue(), selectedPackages)) {
                        continue packageLoop;
                    }
                }
            }

            return true;
        }

        selectedPackages.remove(packageName);
        return false;
    }

    public static <E extends Exception> PackageResolver<E> memoizedPackageResolver(PackageResolver<E> packageResolver) {
        Map<NameVersionSpecPair, Map<Version, Map<String, VersionSpec>>> memos = new HashMap<>();
        return (packageName, versionSpec) -> {
            NameVersionSpecPair key = new NameVersionSpecPair(packageName, versionSpec);
            Map<Version, Map<String, VersionSpec>> result = memos.get(key);
            if (result == null) {
                result = packageResolver.getMatchingEntries(packageName, versionSpec);
                memos.put(key, result);
            }
            return result;
        };
    }

    private static class NameVersionSpecPair {
        private final String name;
        private final VersionSpec versionSpec;

        private NameVersionSpecPair(String name, VersionSpec versionSpec) {
            this.name = name;
            this.versionSpec = versionSpec;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            NameVersionSpecPair that = (NameVersionSpecPair) o;
            return Objects.equals(name, that.name) && Objects.equals(versionSpec, that.versionSpec);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, versionSpec);
        }
    }

    public interface PackageResolver<E extends Exception> {
        Map<Version, Map<String, VersionSpec>> getMatchingEntries(String packageName, VersionSpec versionSpec) throws E;
    }
}

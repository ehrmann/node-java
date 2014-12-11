package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingVersionException;
import com.davidehrmann.nodejava.packagemanager.Repository;
import com.davidehrmann.nodejava.packagemanager.RepositoryException;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.util.CollectorsExtra;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

public class NpmCouchRepository implements Repository {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final String baseUrl;

    public NpmCouchRepository(String baseUrl) throws MalformedURLException {
        new URL(baseUrl);
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        new URL(baseUrl);
        this.baseUrl = baseUrl;
    }

    @Override
    public Set<Object> getAvailablePackages() {
        return null;
    }

    @Override
    public Map.Entry<Version, NpmRegistryEntry.VersionInfo> getMatchingEntry(String packageName, VersionSpec versionSpec) throws NoMatchingPackageException, NoMatchingVersionException, RepositoryException {
        NavigableMap<Version, NpmRegistryEntry.VersionInfo> entries = getMatchingEntries(packageName, versionSpec);

        // TODO: stable/beta/latest
        // if (versionSpec.equals(VersionSpec.LATEST_RELEASE)) { }
        if (VersionSpec.LATEST.equals(versionSpec) && !entries.isEmpty()) {
            return entries.lastEntry();
        }

        Map.Entry<Version, NpmRegistryEntry.VersionInfo> bestMatch = null;
        for (Map.Entry<Version, NpmRegistryEntry.VersionInfo> entry : entries.descendingMap().entrySet()) {
            if (versionSpec.matches(entry.getKey())) {
                bestMatch = entry;
                break;
            }
        }

        if (bestMatch == null) {
            throw new NoMatchingVersionException(packageName, entries.keySet(), versionSpec);
        }

        return bestMatch;
    }

    @Override
    public NavigableMap<Version, NpmRegistryEntry.VersionInfo> getMatchingEntries(String packageName, VersionSpec versionSpec) throws RepositoryException {
        URL url;
        try {
            url = new URL(baseUrl + URLEncoder.encode(packageName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Map<String, Version> distTags = Collections.checkedMap(new HashMap<>(), String.class, Version.class);
        Map<Version, URL> urls = new HashMap<>();

        NpmRegistryEntry entry;
        try {
            return OBJECT_MAPPER.readValue(url, NpmRegistryEntry.class).getVersions().entrySet().stream()
                    .filter(e -> versionSpec.matches(e.getKey()))
                    .collect(CollectorsExtra.toNavigableMap(Map.Entry::getKey, Map.Entry::getValue))
                    .descendingMap();
        } catch (FileNotFoundException e) {
            // TODO: or throw a different exception?
            return Collections.emptyNavigableMap();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
}
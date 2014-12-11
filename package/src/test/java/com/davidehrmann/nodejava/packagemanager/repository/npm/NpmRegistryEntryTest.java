package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NpmRegistryEntryTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void test() throws IOException {
        NpmRegistryEntry npmRegistryEntry;
        try (InputStream in = new GZIPInputStream(NpmRegistryEntryTest.class.getResourceAsStream("less.json.gz"))) {
            npmRegistryEntry = OBJECT_MAPPER.readValue(in, NpmRegistryEntry.class);
        }

        Map<String, Version> expectedDistTags = new ImmutableMap.Builder<String, Version>()
                .put("stable", Version.fromString("1.7.4"))
                .put("beta", Version.fromString("1.7.4"))
                .put("latest", Version.fromString("1.7.4"))
                .build();

        NavigableSet<Version> expectedVersions = Stream.of(
                "1.0.10", "1.0.11", "1.0.14", "1.0.18", "1.0.19", "1.0.21", "1.0.32", "1.0.36", "1.0.5",
                "1.0.40", "1.0.41", "1.0.44", "1.1.0", "1.1.1", "1.1.2", "1.1.4", "1.1.5", "1.1.6", "1.2.0",
                "1.2.1", "1.2.2", "1.3.0", "1.3.1", "1.3.2", "1.3.3", "1.4.0-b1", "1.4.0-b2", "1.4.0-b3",
                "1.4.0-b4", "1.4.0", "1.4.1", "1.4.2", "1.5.0-b1", "1.5.0-b2", "1.5.0-b3", "1.5.0-b4",
                "1.5.0", "1.5.1", "1.6.0", "1.6.1", "1.6.2", "1.6.3", "1.7.0", "1.7.1", "1.7.3", "1.7.4"
        ).map(Version::fromString).collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedDistTags, npmRegistryEntry.getDistTags());
        assertEquals(expectedVersions, npmRegistryEntry.getVersions().keySet());
        assertEquals(
                Collections.singletonMap("ycssmin", VersionSpec.fromString(">=1.0.1")),
                npmRegistryEntry.getVersions().get(Version.fromString("1.3.2")).getDependencies()
        );

        assertEquals(
                Collections.singletonMap("diff", VersionSpec.fromString("~1.0.x")),
                npmRegistryEntry.getVersions().get(Version.fromString("1.3.2")).getDevDependencies()
        );

        assertEquals(
                Collections.singletonMap("ycssmin", VersionSpec.fromString(">=1.0.1")),
                npmRegistryEntry.getVersions().get(Version.fromString("1.3.2")).getOptionalDependencies()
        );

        assertTrue(npmRegistryEntry.getVersions().keySet().containsAll(npmRegistryEntry.getDistTags().values()));

        for (Map.Entry<Version, NpmRegistryEntry.VersionInfo> entry : npmRegistryEntry.getVersions().entrySet()) {
            assertEquals("/registry/less/less-" + entry.getKey() + ".tgz", entry.getValue().getDistInfo().getTarball().getPath());
            assertEquals(20, entry.getValue().getDistInfo().getShasum().length);
        }
    }
}

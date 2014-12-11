package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.davidehrmann.nodejava.util.CollectorsExtra;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmRegistryEntry {
    @JsonProperty("dist-tags")
    @JsonDeserialize(keyAs = String.class, contentUsing = VersionDeserializer.class)
    private Map<String, Version> distTags;

    @JsonProperty("versions")
    @JsonDeserialize(keyUsing = VersionKeyDeserializer.class, contentAs = VersionInfo.class)
    private Map<Version, VersionInfo> versions;

    // Add these?
    // String name;
    // String description;

    public Map<String, Version> getDistTags() {
        return Collections.unmodifiableMap(distTags);
    }

    public NavigableMap<Version, VersionInfo> getVersions() {
        // TODO: cache
        return Collections.unmodifiableNavigableMap(versions.entrySet()
                .stream()
                .filter(e -> e.getKey() != null)
                .collect(CollectorsExtra.toNavigableMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionInfo {
        private final DistInfo distInfo;
        private final Map<String, VersionSpec> dependencies;
        private final Map<String, VersionSpec> devDependencies;
        private final Map<String, VersionSpec> optionalDependencies;

        public VersionInfo(
                @JsonProperty("dist") DistInfo distInfo,
                @JsonProperty("dependencies") @JsonDeserialize(keyAs = String.class, contentUsing =  VersionSpecDeserializer.class) Map<String, VersionSpec> dependencies,
                @JsonProperty("devDependencies") @JsonDeserialize(keyAs = String.class, contentUsing =  VersionSpecDeserializer.class) Map<String, VersionSpec> devDependencies,
                @JsonProperty("optionalDependencies") @JsonDeserialize(keyAs = String.class, contentUsing =  VersionSpecDeserializer.class) Map<String, VersionSpec> optionalDependencies
                ) {
            this.distInfo = Objects.requireNonNull(distInfo);
            this.dependencies = dependencies != null ? dependencies : Collections.emptyMap();
            this.devDependencies = dependencies != null ? devDependencies : Collections.emptyMap();
            this.optionalDependencies = dependencies != null ? optionalDependencies : Collections.emptyMap();
        }

        public DistInfo getDistInfo() {
            return distInfo;
        }

        public Map<String, VersionSpec> getDependencies() { return dependencies; }

        public Map<String, VersionSpec> getDevDependencies() {
            return devDependencies;
        }

        public Map<String, VersionSpec> getOptionalDependencies() {
            return optionalDependencies;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DistInfo {
        private URL tarball;
        private byte[] shasum;

        public DistInfo(@JsonProperty("tarball") String tarball, @JsonProperty("shasum") String shasum) throws MalformedURLException {
            this.shasum = DatatypeConverter.parseHexBinary(shasum);
            this.tarball = new URL(tarball);
        }

        public URL getTarball() {
            return tarball;
        }

        public byte[] getShasum() {
            return shasum;
        }
    }

    public static class VersionKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String s, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            try {
                return Version.fromString(s);
            } catch (IllegalArgumentException e) {
                //throw new JsonMappingException("Couldn't parse key as a Version", e);
                return null;
            }
        }
    }

    public static class NullDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            jsonParser.skipChildren();
            return null;
        }
    }
}
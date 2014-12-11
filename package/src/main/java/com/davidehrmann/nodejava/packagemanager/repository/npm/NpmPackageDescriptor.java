package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmPackageDescriptor {
    @JsonProperty("optionalDependencies")
    @JsonDeserialize(keyAs = String.class, contentUsing = VersionSpecDeserializer.class)
    private Map<String, VersionSpec> optionalDependencies = new LinkedHashMap<>();

    @JsonProperty("dependencies")
    @JsonDeserialize(keyAs = String.class, contentUsing = VersionSpecDeserializer.class)
    private Map<String, VersionSpec> dependencies = new LinkedHashMap<>();

    @JsonProperty("devDependencies")
    @JsonDeserialize(keyAs = String.class, contentUsing = VersionSpecDeserializer.class)
    private Map<String, VersionSpec> devDependencies = new LinkedHashMap<>();

    @JsonProperty("name")
    private String name;

    @JsonDeserialize(contentUsing = VersionDeserializer.class)
    private Version version;

    public Map<String, VersionSpec> getOptionalDependencies() {
        return Collections.unmodifiableMap(optionalDependencies);
    }

    public Map<String, VersionSpec> getDependencies() {
        return Collections.unmodifiableMap(dependencies);
    }

    public Map<String, VersionSpec> getDevDependencies() {
        return Collections.unmodifiableMap(devDependencies);
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public static class VersionSpecDeserializer extends JsonDeserializer<VersionSpec> {
        @Override
        public VersionSpec deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            try {
                return VersionSpec.fromString(jsonParser.readValueAs(String.class));
            } catch (IllegalArgumentException e) {
                throw new JsonMappingException("Couldn't parse value as a VersionSpec", e);
            }
        }
    }
}

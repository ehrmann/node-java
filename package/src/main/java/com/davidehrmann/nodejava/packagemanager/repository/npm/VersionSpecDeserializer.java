package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class VersionSpecDeserializer extends JsonDeserializer<VersionSpec> {
    @Override
    public VersionSpec deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            return VersionSpec.fromString(jsonParser.readValueAs(String.class));
        } catch (IllegalArgumentException e) {
            return null;
            // throw new JsonMappingException("Couldn't parse value as a Version", e);
        }
    }
}

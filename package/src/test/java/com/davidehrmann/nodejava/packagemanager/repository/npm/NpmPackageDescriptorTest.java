package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class NpmPackageDescriptorTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testPackageDescriptorParsing() throws IOException {
        try (InputStream in = this.getClass().getResourceAsStream("package.json")) {
            NpmPackageDescriptor p = OBJECT_MAPPER.readValue(in, NpmPackageDescriptor.class);
            // TODO:
            System.out.print(p);
        }
    }
}

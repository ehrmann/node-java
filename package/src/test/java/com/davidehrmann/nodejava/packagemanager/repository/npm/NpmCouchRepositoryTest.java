package com.davidehrmann.nodejava.packagemanager.repository.npm;

import com.davidehrmann.nodejava.packagemanager.NoMatchingPackageException;
import com.davidehrmann.nodejava.packagemanager.NoMatchingVersionException;
import com.davidehrmann.nodejava.packagemanager.RepositoryException;
import com.davidehrmann.nodejava.packagemanager.Version;
import com.davidehrmann.nodejava.packagemanager.VersionSpec;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;

public class NpmCouchRepositoryTest {

    @Test
    public void testGetPackage() throws IOException, RepositoryException, NoMatchingVersionException, NoMatchingPackageException {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();

        File less = folder.newFile("less");

        try (FileOutputStream out = new FileOutputStream(less);
             InputStream in = new GZIPInputStream(getClass().getResourceAsStream("less.json.gz"))
        ) {
            IOUtils.copy(in, out);
        }

        NpmCouchRepository repo = new NpmCouchRepository(folder.getRoot().toURI().toURL().toExternalForm());

        Map.Entry<Version, NpmRegistryEntry.VersionInfo> distInfo = repo.getMatchingEntry("less", VersionSpec.fromString("~1.7"));
        assertNotNull(distInfo);
    }

}

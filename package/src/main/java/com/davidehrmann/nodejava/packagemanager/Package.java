package com.davidehrmann.nodejava.packagemanager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Package {
    String getName();

    Version getVersion();

    InputStream openFile(String path) throws FileNotFoundException, FileStoreException, IOException;

    Map<String, VersionSpec> getDependencies();

    // TODO?
    // Get main?  Get other metadata?
}

package com.davidehrmann.nodejava.packagemanager;

import java.util.Set;

public class NoMatchingVersionException extends Exception {

    public NoMatchingVersionException(String packageName, Set<Version> versions, VersionSpec versionSpec) {

    }
}

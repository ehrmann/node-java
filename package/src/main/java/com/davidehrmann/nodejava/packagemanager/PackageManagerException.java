package com.davidehrmann.nodejava.packagemanager;

public class PackageManagerException extends Exception {
    public PackageManagerException() {
    }

    public PackageManagerException(String message) {
        super(message);
    }

    public PackageManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PackageManagerException(Throwable cause) {
        super(cause);
    }
}

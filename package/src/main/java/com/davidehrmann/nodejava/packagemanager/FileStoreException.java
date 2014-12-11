package com.davidehrmann.nodejava.packagemanager;

public class FileStoreException extends Exception {
    public FileStoreException() {
    }

    public FileStoreException(String message) {
        super(message);
    }

    public FileStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStoreException(Throwable cause) {
        super(cause);
    }
}

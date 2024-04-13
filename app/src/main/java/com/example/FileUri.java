package com.example;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import io.github.rosemoe.sora.lsp.editor.completion.LspCompletionItem;

public final class FileUri {

    private final String path;

    public FileUri(String path) {
        this.path = path;
    }
    LspCompletionItem i;

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<FileUri, Integer> versionMap = new HashMap<>();

    public int getVersion() {
        ReadWriteLock notNullLock = lock != null ? lock : new ReentrantReadWriteLock();
        if (lock == null) {
            lock = notNullLock;
        }

        notNullLock.readLock().lock();
        try {
            int version = versionMap.getOrDefault(this, 0);
            version++;
            return version;
        } finally {
            notNullLock.readLock().unlock();
        }
    }

    public void updateVersionMap(int newVersion) {
        lock.writeLock().lock();
        try {
            versionMap.put(this, newVersion);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public File toFile() {
        return new File(toUri());
    }

    public URI toUri() {
        return URI.create(toFileUri());
    }

    private String toFileUri() {
        // Assuming that the path is already a valid file URI
        return "file://" + path;
    }

    // Optionally, override toString, equals, and hashCode for better immutability support

    @Override
    public String toString() {
        return "FileUri{" +
            "path='" + path + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileUri fileUri = (FileUri) o;
        return path.equals(fileUri.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}


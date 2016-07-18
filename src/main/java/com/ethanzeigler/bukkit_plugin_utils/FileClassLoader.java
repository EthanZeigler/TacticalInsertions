package com.ethanzeigler.bukkit_plugin_utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileClassLoader extends ClassLoader {

    private Path path;

    /**
     * Creates a new class loader using the <tt>ClassLoader</tt> returned by
     * the method {@link #getSystemClassLoader()
     * <tt>getSystemClassLoader()</tt>} as the parent class loader.
     * <p>
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @throws SecurityException If a security manager exists and its
     *                           <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *                           of a new class loader.
     */
    public FileClassLoader(Path path) {
        // check is path is valid
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("The path must be a valid directory");
        }
        this.path = path;
    }

    @Override
    public URL getResource(String name) {
        Path resPath = path.resolve(name);

        if(Files.exists(resPath)) {
            // valid file
            try {
                return resPath.toUri().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // file does not exist
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (getResource(name) != null) {
            // valid resource
            try {
                return Files.newInputStream(path.resolve(name), StandardOpenOption.READ);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // resource does not exist
            return null;
        }
    }
}

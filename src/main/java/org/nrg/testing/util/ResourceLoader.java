package org.nrg.testing.util;

import org.nrg.testing.xnat.conf.Settings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLoader {

    public static File copyAndGetResource(String fileName) {
        Path destination = Paths.get(Settings.TEMP_SUBDIR, fileName);
        try {
            File file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).toURI());
            Files.copy(file.toPath(), destination);
        } catch (Exception e) {
            try {
                Files.copy(ResourceLoader.class.getClassLoader().getResourceAsStream(fileName), destination);
            } catch (Exception e2) {
                throw new RuntimeException("Failed to read resource: " + fileName);
            }
        }
        return destination.toFile();
    }

}

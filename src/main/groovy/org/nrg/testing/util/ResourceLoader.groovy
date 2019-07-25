package org.nrg.testing.util

import org.nrg.testing.FileIOUtils
import org.nrg.testing.xnat.conf.Settings

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ResourceLoader {

    static File copyAndGetResource(String fileName) {
        final Path destination = Paths.get(Settings.TEMP_SUBDIR, fileName)
        try {
            File file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).toURI())
            Files.copy(file.toPath(), destination)
        } catch (Exception ignored) {
            Files.copy(FileIOUtils.loadResourceAsStream(fileName), destination)
        }
        destination.toFile()
    }

}

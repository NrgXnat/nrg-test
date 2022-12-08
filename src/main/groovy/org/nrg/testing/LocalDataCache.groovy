package org.nrg.testing

import org.nrg.testing.xnat.conf.Settings

import java.nio.file.Path
import java.nio.file.Paths

class LocalDataCache {

    public static final String DIR_NAME = 'local_data_cache'
    public static final Path CACHE_PATH = Paths.get(Settings.DATA_LOCATION, DIR_NAME)

    static Path pathTo(Path relativePath) {
        CACHE_PATH.resolve(relativePath)
    }

    static Path pathTo(String relativePath) {
        CACHE_PATH.resolve(relativePath)
    }

    static {
        FileIOUtils.mkdirs(CACHE_PATH)
    }

}

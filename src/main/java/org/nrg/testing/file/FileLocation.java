package org.nrg.testing.file;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class FileLocation {
    
    public static final String CURRENT_DIRECTORY = getCurrentDir();
    public static final String RESOURCE_DIRECTORY = makePath(CURRENT_DIRECTORY, "src", "test", "resources");

    public static String getDataLocation() {
        return makePath(RESOURCE_DIRECTORY, "data");
    }

    public static String getConfigLocation() {
        return makePath(RESOURCE_DIRECTORY, "config");
    }

    public static String getResultLocation() {
        return makePath(CURRENT_DIRECTORY, "target");
    }

    public static String getTimeLogsLocation() {
        return makePath(CURRENT_DIRECTORY, "timeLogs");
    }

    private static String makePath(String... parts) {
        return StringUtils.join(parts, File.separator);
    }
    
    private static String getCurrentDir() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find current directory at test initialization.", e);
        }
    }
    
}

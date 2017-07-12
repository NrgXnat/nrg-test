/*
 * org.nrg.selenium.FileLocation
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:13 PM
 */

package org.nrg.testing.file;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class FileLocation {
    private String currentDirectory;
    public static FileLocation fileLocation = new FileLocation();

    private FileLocation() {
        final File dir = new File(".");
        try {
            currentDirectory = dir.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDataLocation() {
        return makePath(new String[] { currentDirectory, "src", "test", "resources", "data" });
    }

    public String getConfigLocation() {
        return makePath(new String[] { currentDirectory, "src", "test", "resources", "config" });
    }

    public String getResultLocation() {
        return makePath(new String[] { currentDirectory, "target" });
    }

    public String getTimeLogsLocation() {
        return makePath(new String[] { currentDirectory, "timeLogs" });
    }

    private String makePath(Object[] parts) {
        return StringUtils.join(parts, File.separator);
    }
}

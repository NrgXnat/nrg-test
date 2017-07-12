/*
 * MD5_Comparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.processing.files.comparators;

import org.nrg.testing.file.FileIO;

import java.io.File;
import java.io.IOException;

public class MD5_Comparator extends FileComparator {

    private String md5;

    public MD5_Comparator(String md5) {
        this.md5 = md5;
    }

    public String checkFileMatches(File file) {
        String calculatedMD5;
        try {
            calculatedMD5 = FileIO.calculateMD5(file);
            if (!calculatedMD5.equals(md5)) return "MD5 checksum did not match expected value for file: " + file.getAbsolutePath();
        } catch (IOException ioe) {
            return "Could not open file " + file.getAbsolutePath();
        }
        return null;
    }
}

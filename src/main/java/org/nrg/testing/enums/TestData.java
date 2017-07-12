package org.nrg.testing.enums;

import org.nrg.testing.xnat.conf.Settings;

import java.io.File;
import java.nio.file.Paths;

public enum TestData {
    ANON_SESSION ("anonymizationSession"),
    JPEGLOSSLESS_2000 ("jpeglosslessTestSession"),
    PETMR_DATA ("petmrTestData"),
    SAMPLE_1 ("sample1"),
    SAMPLE_2 ("sample2"),
    SIF_SESSION ("sifSession"),
    SIMPLE_PET ("simplePET"),
    XSYNC_DATA ("xsync_test_data"),
    NONE (null);

    private final String name;

    TestData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getZipName() {
        return (name == null) ? null : name + ".zip";
    }

    public File toFile() {
        return Paths.get(Settings.DATA_LOCATION, getZipName()).toFile();
    }

}

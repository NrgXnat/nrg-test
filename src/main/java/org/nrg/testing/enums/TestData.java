package org.nrg.testing.enums;

import org.nrg.testing.xnat.conf.Settings;

import java.io.File;
import java.nio.file.Paths;

public enum TestData {
    ANON_SESSION ("anonymizationSession"),
    ANON_2 ("anon2"),
    JPEGLOSSLESS_2000 ("jpeglosslessTestSession"),
    PETMR_DATA ("petmrTestData"),
    SAMPLE_1 ("sample1", "1.3.12.2.1107.5.2.32.35177.30000006121218324675000000034"),
    SAMPLE_2 ("sample2"),
    SIF_SESSION ("sifSession"),
    SIMPLE_PET ("simplePET"),
    XSYNC_DATA ("xsync_test_data"),
    DICOM_WEB_CT1 ("dicom_web_ct1", "1.3.6.1.4.1.14519.5.2.1.1188.4001.213420711084714071744561785405"),
    DICOM_WEB_CT2 ("dicom_web_ct2", "1.3.6.1.4.1.14519.5.2.1.2783.4001.264840947689124794109906553823"),
    DICOM_WEB_CT3 ("dicom_web_ct3", "1.3.6.1.4.1.14519.5.2.1.2783.4001.836352507614755874726843241657"),
    DICOM_WEB_CT4 ("dicom_web_ct4", "1.3.6.1.4.1.14519.5.2.1.5826.4001.115716244521609483756859197133"),
    DICOM_WEB_CT5 ("dicom_web_ct5", "1.3.6.1.4.1.14519.5.2.1.2783.4001.810666279648825178959720680757"),
    DICOM_WEB_CT6 ("dicom_web_ct6", "1.3.6.1.4.1.14519.5.2.1.2783.4001.106327497160048054133687322917"),
    DICOM_WEB_CT7 ("dicom_web_ct7", "1.3.6.1.4.1.14519.5.2.1.4591.4001.303546620668041504180142477000"),
    DICOM_WEB_CT8 ("dicom_web_ct8", "1.3.6.1.4.1.14519.5.2.1.2783.4001.613594630078243374874781046903"),
    NONE (null);

    private final String name;
    private final String studyInstanceUid;

    TestData(String name, String studyInstanceUid) {
        this.name = name;
        this.studyInstanceUid = studyInstanceUid;
    }

    TestData(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public String getZipName() {
        return (name == null) ? null : name + ".zip";
    }

    public String getStudyInstanceUid() {
        return studyInstanceUid;
    }

    public File toFile() {
        return Paths.get(Settings.DATA_LOCATION, getZipName()).toFile();
    }

    public File toDirectory() {
        return Paths.get(Settings.DATA_LOCATION, name).toFile();
    }

}

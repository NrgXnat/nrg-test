package org.nrg.testing.enums

import groovy.util.logging.Log4j
import org.nrg.testing.FileIOUtils
import org.nrg.testing.LocalDataCache
import org.nrg.testing.XnatDownloadServerClient

import static org.testng.AssertJUnit.assertTrue

@Log4j
enum TestData {
    ANON_SESSION ('anonymizationSession', '1.3.12.2.1107.5.2.32.35177.30000006121218324675000000034'),
    ANON_2 ('anon2'),
    ANON_DUPLICATE_PRIVATE_TAG ('invalid_duplicate_private_header', '1.3.6.1.4.1.14519.5.2.1.9823.8001.124757823774264144507589669495'),
    JPEGLOSSLESS_2000 ('jpeglosslessTestSession', '1.2.840.113654.2.45.6231.166972699289982407626220940255566350778'),
    PETMR_DATA ('petmrTestData'),
    SAMPLE_1 ('sample1', '1.3.12.2.1107.5.2.32.35177.30000006121218324675000000034'),
    SAMPLE_1_SCAN_4 ('sample1_scan4'),
    SAMPLE_1_SCAN_5 ('sample1_scan5'),
    SAMPLE_1_SCAN_6 ('sample1_scan6'),
    SAMPLE_2 ('sample2'),
    SIF_SESSION ('sifSession'),
    SIMPLE_PET ('simplePET'),
    XSYNC_DATA ('xsync_test_data'),
    NIFTI_FILE ('Nifti_file'),
    MIXED_FRAME_STUDY ('mixed_frame_study', '1.2.276.0.7230010.3.1.2.0.40812.1517409389.348186'),
    BIG_ENDIAN ('dicom_web_big_endian', '1.2.276.0.7230010.3.1.2.0.35635.1517265970.592487'),
    DICOM_WEB_CT1 ('dicom_web_ct1', '1.3.6.1.4.1.14519.5.2.1.1188.4001.213420711084714071744561785405'),
    DICOM_WEB_CT2 ('dicom_web_ct2', '1.3.6.1.4.1.14519.5.2.1.2783.4001.264840947689124794109906553823'),
    DICOM_WEB_CT3 ('dicom_web_ct3', '1.3.6.1.4.1.14519.5.2.1.2783.4001.836352507614755874726843241657'),
    DICOM_WEB_CT4 ('dicom_web_ct4', '1.3.6.1.4.1.14519.5.2.1.5826.4001.115716244521609483756859197133'),
    DICOM_WEB_CT5 ('dicom_web_ct5', '1.3.6.1.4.1.14519.5.2.1.2783.4001.810666279648825178959720680757'),
    DICOM_WEB_CT6 ('dicom_web_ct6', '1.3.6.1.4.1.14519.5.2.1.2783.4001.106327497160048054133687322917'),
    DICOM_WEB_CT7 ('dicom_web_ct7', '1.3.6.1.4.1.14519.5.2.1.4591.4001.303546620668041504180142477000'),
    DICOM_WEB_CT8 ('dicom_web_ct8', '1.3.6.1.4.1.14519.5.2.1.2783.4001.613594630078243374874781046903'),
    DICOM_WEB_MR1 ('dicom_web_mr1', '1.3.6.1.4.1.14519.5.2.1.3344.2526.581682987737689932129855326527'),
    DICOM_WEB_MR2 ('dicom_web_mr2', '1.3.6.1.4.1.14519.5.2.1.3344.2526.483299392646588167902364624064'),
    DICOM_WEB_MR3 ('dicom_web_mr3', '1.3.6.1.4.1.14519.5.2.1.3344.2526.241249471882503252500963678515'),
    DICOM_WEB_MR4 ('dicom_web_mr4', '1.3.6.1.4.1.14519.5.2.1.3344.2526.170284863798588304106756296232'),
    DICOM_WEB_MRPR ('Watermelon', '1.3.46.670589.11.5730.5.0.1744.2010043012343685002'),
    DICOM_WEB_PETCT1 ('0522c0001_0000', '1.3.6.1.4.1.14519.5.2.1.5099.8010.199920086920823171706454903251'),
    DICOM_WEB_PETCT2_PET ('0522c0001_0001_PET', '1.3.6.1.4.1.14519.5.2.1.5099.8010.256809878238026661650178294515'),
    DICOM_WEB_PETCT2_CT ('0522c0001_0001_CT', '1.3.6.1.4.1.14519.5.2.1.5099.8010.256809878238026661650178294515'),
    DICOM_WEB_CTRT1 ('0522c0001_0003', '1.3.6.1.4.1.22213.2.26555'),
    DICOM_WEB_CTRT2 ('0522c0002', '1.3.6.1.4.1.22213.2.26556'),
    DICOM_WEB_MULTIFRAME_CT ('multiframe_CT', '1.3.6.1.4.1.5962.1.2.10.1166562673.14401'),
    DICOM_WEB_PETMR1 ('STS_001_A', '1.3.6.1.4.1.14519.5.2.1.5168.1900.980314374925518526455629963929'),
    DICOM_WEB_PETMR2_MR ('STS_001_B_MR', '1.3.6.1.4.1.14519.5.2.1.5168.1900.312508428193476302553228969583'),
    DICOM_WEB_PETMR2_PT ('STS_001_B_PT', '1.3.6.1.4.1.14519.5.2.1.5168.1900.312508428193476302553228969583'),
    DICOM_WEB_PETMR3 ('STS_040', '1.3.6.1.4.1.14519.5.2.1.5168.1900.115384553692459441419317900120'),
    DICOM_WEB_PETMR4_MR ('STS_045_MR', '1.3.6.1.4.1.14519.5.2.1.5168.1900.884190365560171635301842723223'),
    DICOM_WEB_PETMR4_PT ('STS_045_PT', '1.3.6.1.4.1.14519.5.2.1.5168.1900.884190365560171635301842723223'),
    DICOM_WEB_MISSINGNO_MR ('900-00-5346', '1.3.6.1.4.1.9328.50.50.100305757391022324960696127128274390925'),
    DICOM_WEB_MG ('dicom_web_mg', '1.3.6.1.4.1.14519.5.2.1.4792.2001.174604453911712310689369687743'),
    EXTRACTION_DIFFUSION ('diffusion'),
    EXTRACTION_MR ('extraction_mr'),
    EXTRACTION_CT ('extraction_ct'),
    EXTRACTION_OPT ('extraction_opt'),
    NONE (null)

    private final String name
    private final String studyInstanceUid

    TestData(String name, String studyInstanceUid) {
        this.name = name
        this.studyInstanceUid = studyInstanceUid
    }

    TestData(String name) {
        this(name, null)
    }

    String getName() {
        name
    }

    String getZipName() {
        (name == null) ? null : name + '.zip'
    }

    String getStudyInstanceUid() {
        studyInstanceUid
    }

    File toFile() {
        LocalDataCache.pathTo(zipName).toFile()
    }

    File toDirectory() {
        LocalDataCache.pathTo(name).toFile()
    }

    void download() {
        if (this != NONE) {
            final File possiblyCachedZip = toFile()
            if (possiblyCachedZip.exists()) {
                if (possiblyCachedZip.length() < 1000) { // it's less than 1 KB (e.g. probably empty, no test data will be this small)
                    assertTrue(possiblyCachedZip.delete())
                } else {
                    log.info("I already have the ${zipName} test data. No need to download again!")
                    return
                }
            }
            cacheClient().downloadToFile(zipName, possiblyCachedZip)
            FileIOUtils.unzip(possiblyCachedZip.parentFile, possiblyCachedZip, true)
        }
    }

    private static XnatDownloadServerClient downloadServerClient

    private static XnatDownloadServerClient cacheClient() {
        if (downloadServerClient == null) {
            downloadServerClient = new XnatDownloadServerClient()
        }
        downloadServerClient
    }

}

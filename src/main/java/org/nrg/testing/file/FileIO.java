package org.nrg.testing.file;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.dcm4che3.data.Tag;
import org.nrg.testing.enums.TestData;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.xnat.dicom.CStore;
import org.nrg.xnat.pogo.Project;
import org.nrg.xnat.pogo.dicom.DicomScpReceiver;
import org.nrg.xnat.util.FileIOUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.testng.AssertJUnit.*;

public class FileIO {

    private static final Logger LOGGER = Logger.getLogger(FileIO.class);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void mkdirs(String path) {
        new File(path).mkdirs();
    }

    public static void rmdir(String path) {
        try {
            File dir = new File(path);
            if (dir.exists()) {
                FileUtils.deleteDirectory(dir);
            }
        } catch (IOException ioe) {
            LOGGER.fatal("Failed to remove directory " + path, ioe);
            throw new RuntimeException("Failed to remove directory.");
        }
    }

    public static File writeExceptionToFile(String dirPath, String fileName, Throwable exception) {
        File file = Paths.get(dirPath, fileName + ".txt").toFile();
        mkdirs(dirPath);
        try {
            PrintWriter exceptionWriter = new PrintWriter(file);
            exception.printStackTrace(exceptionWriter);
            exceptionWriter.close();
            return file;
        } catch (FileNotFoundException e1) {
            LOGGER.warn("Could not create exception log file.", e1);
            return null;
        }
    }

    public static File getDataFile(String filename) {
        final File possibleFile = Paths.get(Settings.DATA_LOCATION, filename).toFile();
        if (possibleFile.exists()) {
            return possibleFile;
        } else {
            return null;
        }
    }

    public static void unzip(Path unzipFolder, File zip) {
        if (!unzipFolder.toFile().isDirectory()) { // refuse to overwrite
            try {
                ZipFile zipFile = new ZipFile(zip);
                zipFile.extractAll(unzipFolder.toFile().toString());
            } catch (ZipException e) {
                e.printStackTrace();
                fail("Failed to unzip test data.");
            }
        }
    }

    public static void unzip(String directoryPath, String zipName) {
        final String unzippedFolder = directoryPath + File.separator + zipName.substring(0, zipName.length() - 4); // we know zip is a .zip
        unzip(Paths.get(unzippedFolder), new File(directoryPath + File.separator + zipName));
    }

    public static String calculateMD5(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        return DigestUtils.md5Hex(inputStream);
    }

    /**
     * Sends a directory containing DICOM files to an XNAT server
     * @param aeTitle Remote AETitle to receive the DICOM
     * @param host Remote host to receive the DICOM
     * @param port Port number for the remote DICOM Receiver
     * @param directory Directory of DICOM
     * @param overwrittenDICOMHeaders A map of keys (DICOM tags in decimal representation of the hex code, e.g. Tag.PatientName) to values (DICOM values, e.g. "MYSTUDY")
     */
    public static void sendDICOM(String aeTitle, String host, Integer port, String directory, Map<Integer, String> overwrittenDICOMHeaders) {
        CStore.to(new DicomScpReceiver().aeTitle((aeTitle == null) ? Settings.DICOM_AETITLE : aeTitle).port((port == null) ? Settings.DICOM_PORT : port).host((host == null) ? Settings.DICOM_HOST : host)).
                directories(Collections.singletonList(Paths.get(Settings.DATA_LOCATION, directory).toFile())).headers(overwrittenDICOMHeaders).send();
    }

    public static void sendDICOM(DicomScpReceiver dicomScpReceiver, String directory, Map<Integer, String> overwrittenDICOMHeaders) {
        sendDICOM(dicomScpReceiver.getAeTitle(), dicomScpReceiver.getHost(), dicomScpReceiver.getPort(), directory, overwrittenDICOMHeaders);
    }

    public static void sendDICOM(DicomScpReceiver dicomScpReceiver, TestData testData, Map<Integer, String> overwrittenDICOMHeaders) {
        sendDICOM(dicomScpReceiver, testData.getName(), overwrittenDICOMHeaders);
    }

    public static void sendDICOM(String directory, Map<Integer, String> overwrittenDICOMHeaders) {
        sendDICOM(null, null, null, directory, overwrittenDICOMHeaders);
    }

    public static void sendDICOM(TestData testData, Map<Integer, String> overwrittenDICOMHeaders) {
        sendDICOM(testData.getName(), overwrittenDICOMHeaders);
    }

    public static void sendDICOMToProject(String directory, Project project) {
        sendDICOM(directory, Collections.singletonMap(Tag.StudyDescription, project.getId()));
    }

    public static void sendDICOMToProject(TestData testData, Project project) {
        sendDICOMToProject(testData.getName(), project);
    }

    public static void getTestData(String testDataName) {
        // Heavily adapted from: http://stackoverflow.com/a/16913150
        final String TEST_DATA_FULL_PATH = Settings.DATA_LOCATION + File.separator + testDataName;
        File testDataFile = new File(TEST_DATA_FULL_PATH);
        if (testDataFile.exists()) {
            if (testDataFile.length() < 10000) {
                // it's less than 10kb (e.g. probably empty, no test data will be this small)
                assertTrue(testDataFile.delete());
            } else {
                LOGGER.info("I already have the " + testDataName + " test data. No need to download again!");
                return;
            }
        }

        try {
            FTPClient ftp = new FTPClient();
            ftp.connect("ftp.nrg.wustl.edu");

            if (!ftp.login("anonymous", Settings.EMAIL)) {
                ftp.logout();
            }

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                fail("Failed to get test data from NRG FTP server.");
            }
            ftp.enterLocalPassiveMode();
            ftp.setFileTransferMode(FTPClient.BLOCK_TRANSFER_MODE);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            OutputStream output = new FileOutputStream(new File(TEST_DATA_FULL_PATH));
            LOGGER.info("Downloading " + testDataName + "...");
            ftp.retrieveFile("/pub/data/" + testDataName, output);
            reply = ftp.getReplyCode();
            output.close();
            ftp.logout();
            ftp.disconnect();
            assertFalse("Failed to find " + testDataName + " data on FTP server.", reply == 550);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Failed to get " + testDataName + " data from NRG FTP server.");
        }
    }

    public static String readDataFile(String filename) {
        return FileIOUtils.readFile(getDataFile(filename));
    }

}

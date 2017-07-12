package org.nrg.testing.dicom;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DicomLibrary {

    public static final DicomElement ACCESSION_NUMBER = new DicomElement("0008,0050", "Accession Number");
    public static final DicomElement MANUFACTURER = new DicomElement("0008,0070", "Manufacturer");
    public static final DicomElement REFERRING_PHYSICIANS_NAME = new DicomElement("0008,0090", "Referring Physician's Name");
    public static final DicomElement REFERRING_PHYSICIANS_ADDRESS = new DicomElement("0008,0092", "Referring Physician's Address");
    public static final DicomElement CODE_VALUE = new DicomElement("0008,0100", "Code Value");
    public static final DicomElement CODE_MEANING = new DicomElement("0008,0104", "Code Meaning");
    public static final DicomElement STATION_NAME = new DicomElement("0008,1010", "Station Name");
    public static final DicomElement STUDY_DESCRIPTION = new DicomElement("0008,1030", "Study Description");
    public static final DicomElement INSTITUTIONAL_DEPARTMENT_NAME = new DicomElement("0008,1040", "Institutional Department Name");
    public static final DicomElement PERFORMING_PHYSICIANS_NAME = new DicomElement("0008,1050", "Performing Physician's Name");
    public static final DicomElement REFERENCED_SOP_INSTANCE_UID = new DicomElement("0008,1155", "Referenced SOP Instance UID");
    public static final DicomElement PATIENTS_NAME = new DicomElement("0010,0010", "Patient's Name");
    public static final DicomElement PATIENT_ID = new DicomElement("0010,0020", "Patient ID");
    public static final DicomElement PATIENTS_SEX = new DicomElement("0010,0040", "Patient's Sex");
    public static final DicomElement ALLERGIES = new DicomElement("0010,2110", "Allergies");
    public static final DicomElement OCCUPATION = new DicomElement("0010,2180", "Occupation");
    public static final DicomElement PATIENT_COMMENTS = new DicomElement("0010,4000", "Patient Comments");
    public static final DicomElement BODY_PART_EXAMINED = new DicomElement("0018,0015", "Body Part Examined");
    public static final DicomElement IMAGING_FREQUENCY = new DicomElement("0018,0084", "Imaging Frequency");
    public static final DicomElement IMAGED_NUCLEUS = new DicomElement("0018,0085", "Imaged Nucleus");
    public static final DicomElement MAGNETIC_FIELD_STRENGTH = new DicomElement("0018,0087", "Magnetic Field Strength");
    public static final DicomElement DETECTOR_ID = new DicomElement("0018,700A", "Detector ID");
    public static final DicomElement STUDY_INSTANCE_UID = new DicomElement("0020,000D", "Study Instance UID");
    public static final DicomElement PRIVATE_PSEUDO_MODALITY = new DicomElement("0029,1018", "?");
    public static final DicomElement PRIVATE_PSEUDO_STUDY_DATE = new DicomElement("0029,1019", "?");
    public static final DicomElement PRIVATE_UNKNOWN_HEX = new DicomElement("0029,1020", "Siemens CSA Image Series Shadow Header");
    public static final DicomElement STUDY_COMMENTS = new DicomElement("0032,4000", "Study Comments");

    public static String dicomHeaderMapToString(Map<DicomElement, String> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) return "";
        List<String> headerMapPairs = new ArrayList<>();
        for (Map.Entry<DicomElement, String> entry : headerMap.entrySet()) {
            headerMapPairs.add(String.format("%s := '%s'", entry.getKey().getDicomTag(), entry.getValue()));
        }
        return Joiner.on(", ").join(headerMapPairs);
    }



}

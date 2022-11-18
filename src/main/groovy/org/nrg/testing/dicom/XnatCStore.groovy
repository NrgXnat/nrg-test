package org.nrg.testing.dicom

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.StandardElementDictionary
import org.dcm4che3.data.Tag
import org.nrg.testing.enums.TestData
import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.dicom.CStore
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.dicom.DicomScpReceiver

import java.nio.file.Paths

class XnatCStore {

    private final CStore cstoreSpec
    private static final int PROJECT_ROUTING_HEADER = Tag.StudyDescription

    XnatCStore(String host, Integer port, String aeTitle, String callingAETitle) {
        cstoreSpec = CStore.to(
                aeTitle ?: Settings.DICOM_AETITLE,
                host ?: Settings.DICOM_HOST,
                port ?: Settings.DICOM_PORT,
                callingAETitle ?: Settings.CALLING_AE_TITLE
        )
    }

    XnatCStore(DicomScpReceiver dicomScpReceiver) {
        this(dicomScpReceiver.host, dicomScpReceiver.port, dicomScpReceiver.aeTitle, null)
    }

    XnatCStore(DicomScpReceiver dicomScpReceiver, String callingAETitle) {
        this(dicomScpReceiver.host, dicomScpReceiver.port, dicomScpReceiver.aeTitle, callingAETitle)
    }

    XnatCStore() {
        this(null, null, null, null)
    }

    XnatCStore data(File directory) {
        cstoreSpec.directory(directory)
        this
    }

    XnatCStore data(String directory) {
        data(Paths.get(Settings.DATA_LOCATION, directory).toFile())
    }

    XnatCStore data(TestData testData) {
        data(testData.name)
    }

    XnatCStore overwrittenHeaders(Map<Integer, String> headers) {
        cstoreSpec.headers(headers)
        this
    }

    void sendDICOM() {
        cstoreSpec.send()
    }

    void sendDICOMToProject(Project project) {
        final Attributes headers = cstoreSpec.headers
        if (headers == null) {
            overwrittenHeaders([(PROJECT_ROUTING_HEADER) : project.id])
        } else {
            headers.setString(PROJECT_ROUTING_HEADER, StandardElementDictionary.INSTANCE.vrOf(PROJECT_ROUTING_HEADER), project.id)
        }
        sendDICOM()
    }

}

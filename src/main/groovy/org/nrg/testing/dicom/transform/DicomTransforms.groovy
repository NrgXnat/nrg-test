package org.nrg.testing.dicom.transform

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.DatasetWithFMI
import org.dcm4che3.data.Tag
import org.dcm4che3.data.VR
import org.dcm4che3.util.UIDUtils
import org.nrg.testing.DicomUtils

class DicomTransforms {

    public static final TransformFunction REMAP_SOP_INSTANCE_UID = TransformFunction.simple(
            dicom -> dicom.getDataset().setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID())
    )
    public static final TransformFunction REMAP_UIDS = TransformFunction.composition(
            REMAP_SOP_INSTANCE_UID,
            TransformFunction.strictlyTransformative(listOfDicom -> {
                final Map<String, String> studyMap = [:]
                final Map<String, String> seriesMap = [:]
                listOfDicom.each { instance ->
                    final Attributes dataset = instance.dataset
                    final String currentStudyInstanceUid = dataset.getString(Tag.StudyInstanceUID)
                    final String currentSeriesInstanceUid = dataset.getString(Tag.SeriesInstanceUID)
                    if (!studyMap.containsKey(currentStudyInstanceUid)) {
                        studyMap.put(currentStudyInstanceUid, UIDUtils.createUID())
                    }
                    dataset.setString(Tag.StudyInstanceUID, VR.UI, studyMap.get(currentStudyInstanceUid))
                    if (!seriesMap.containsKey(currentSeriesInstanceUid)) {
                        seriesMap.put(currentSeriesInstanceUid, UIDUtils.createUID())
                    }
                    dataset.setString(Tag.SeriesInstanceUID, VR.UI, seriesMap.get(currentSeriesInstanceUid))
                    dataset.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID())
                }
            })
    )

    static final TransformFunction duplicateInstance(int numInstances) {
        TransformFunction.composition(
                TransformFunction.generalTransform({ listOfDicom ->
                    if (listOfDicom.size() > 1) {
                        throw new UnsupportedOperationException('This transform expects to take only a single DICOM instance')
                    }
                    (0 ..< numInstances).collect { index ->
                        final DatasetWithFMI clone = DicomUtils.clone(listOfDicom[0])
                        clone.dataset.setInt(Tag.InstanceNumber, VR.IS, index)
                        clone
                    }
                }), // turn 1 instance into N instances with unique Instance Number...and then run those through SOP Instance UID remap
                REMAP_SOP_INSTANCE_UID
        )
    }

}

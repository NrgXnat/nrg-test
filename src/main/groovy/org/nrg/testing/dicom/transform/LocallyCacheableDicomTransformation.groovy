package org.nrg.testing.dicom.transform

import org.dcm4che3.data.Attributes
import org.dcm4che3.data.DatasetWithFMI
import org.dcm4che3.data.VR
import org.dcm4che3.util.UIDUtils
import org.nrg.testing.DicomUtils
import org.nrg.testing.FileIOUtils
import org.nrg.testing.enums.TestData
import org.nrg.testing.xnat.conf.Settings

import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipFile

class LocallyCacheableDicomTransformation {

    String identifier
    TestData baseData
    boolean produceOverallZip
    List<DicomTransformation> transformations
    private static final int FOLDERS_PER_LAYER = 100
    private static final int FILES_PER_FOLDER = 1000

    LocallyCacheableDicomTransformation(String identifier) {
        this.identifier = identifier
    }

    LocallyCacheableDicomTransformation data(TestData data) {
        baseData = data
        this
    }

    LocallyCacheableDicomTransformation createZip() {
        produceOverallZip = true
        this
    }

    LocallyCacheableDicomTransformation transformations(DicomTransformation... transformations) {
        this.transformations = transformations.toList()
        this
    }

    Path baseLevelDir() {
        Paths.get(Settings.DATA_LOCATION, identifier)
    }

    Path locateBaseDirForTransformedData(DicomTransformation transformation) {
        locateBaseDirForTransformedData(transformation.identifier)
    }

    Path locateBaseDirForTransformedData(String identifier) {
        baseLevelDir().resolve('data').resolve(identifier)
    }

    Path locateZipForIndividualTransformation(DicomTransformation transformation) {
        locateBaseDirForTransformedData(transformation.identifier)
    }

    Path locateZipForIndividualTransformation(String identifier) {
        locateBaseDirForTransformedData(identifier).resolve('data.zip')
    }

    Path locateDataForIndividualTransformationInstance(DicomTransformation transformation, int transformationCount = 0) {
        locateDataForIndividualTransformationInstance(transformation.identifier, transformationCount)
    }

    Path locateDataForIndividualTransformationInstance(String identifier, int transformationCount = 0) {
        final Path layer = locateBaseDirForTransformedData(identifier).resolve('layer' + transformationCount.intdiv(FOLDERS_PER_LAYER))
        if (transformationCount % FOLDERS_PER_LAYER == 0) {
            FileIOUtils.mkdirs(layer)
        }
        final Path iteration = layer.resolve('iteration_' + transformationCount)
        FileIOUtils.mkdirs(iteration)
        iteration
    }

    void build() {
        final Path completionMarker = baseLevelDir().resolve('README.txt')
        if (completionMarker.toFile().exists()) {
            return
        }

        final ZipFile zipFile = new ZipFile(baseData.toFile())
        final List<DatasetWithFMI> sourceDicomInstances = zipFile.entries().toList().findResults { zipEntry ->
            !zipEntry.directory ? DicomUtils.readDicom(zipFile.getInputStream(zipEntry)) : null
        }

        transformations.each { transformation ->
            final List<DatasetWithFMI> copyOfSource = new ArrayList<>(sourceDicomInstances)
            final List<DatasetWithFMI> postFilter = transformation.prefilter ? transformation.prefilter.apply(copyOfSource) : copyOfSource
            transformation.transformationCount.times { index ->
                final Path individualIterationPath = locateDataForIndividualTransformationInstance(transformation, index)
                transformation.transformFunction.apply(postFilter).eachWithIndex { instance, fileIndex -> // assumption here is that we don't need to clone again after filtering
                    final Path subfolder = individualIterationPath.resolve('subfolder' + fileIndex.intdiv(FILES_PER_FOLDER))
                    if (fileIndex % FILES_PER_FOLDER == 0) {
                        FileIOUtils.mkdirs(subfolder)
                    }
                    DicomUtils.writeDicomToFile(instance, subfolder.resolve("${fileIndex}.dcm").toFile())
                }
            }
            // TODO: zip an individual transformation
        }
        // TODO: zip the whole thing

       completionMarker.toFile() << 'This marker exists to show that DICOM data has been produced locally for a test. Please do not mess with the data in this directory if you wish to run the tests successfully.'
    }

    private static List<DatasetWithFMI> clone(List<DatasetWithFMI> dicomInstances) {
        dicomInstances.collect { instance ->
            DicomUtils.clone(instance)
        }
    }

}

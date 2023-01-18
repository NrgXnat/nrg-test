package org.nrg.testing.dicom.transform

import org.apache.commons.io.IOUtils
import org.dcm4che3.data.DatasetWithFMI
import org.nrg.testing.DicomUtils
import org.nrg.testing.FileIOUtils
import org.nrg.testing.LocalDataCache
import org.nrg.testing.enums.TestData

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

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

    LocallyCacheableDicomTransformation simpleTransform(TransformFunction transformFunction) {
        transformations(
                new DicomTransformation(identifier).transformFunction(transformFunction)
        )
    }

    Path baseLevelDir() {
        LocalDataCache.pathTo(identifier)
    }

    Path locateOverallZip() {
        LocalDataCache.pathTo("${identifier}.zip")
    }

    Path locateBaseDirForTransformedData(DicomTransformation transformation) {
        locateBaseDirForTransformedData(transformation.identifier)
    }

    Path locateBaseDirForTransformedData(String identifier) {
        baseLevelDir().resolve('data').resolve(identifier)
    }

    Path locateZipForIndividualTransformation(DicomTransformation transformation) {
        locateZipForIndividualTransformation(transformation.identifier)
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

        final List<DatasetWithFMI> sourceDicomInstances = readBaseData()
        final List<File> filesForOverallZip = []

        transformations.each { transformation ->
            final List<File> filesForTransformation = []
            final List<DatasetWithFMI> copyOfSource = new ArrayList<>(sourceDicomInstances) // each transformation needs the full source list
            final List<DatasetWithFMI> postFilter = transformation.prefilter ? transformation.prefilter.apply(copyOfSource) : copyOfSource // ... but we assume here at least the the prefilter won't modify instances directly
            transformation.transformationCount.times { index ->
                final Path individualIterationPath = locateDataForIndividualTransformationInstance(transformation, index)
                transformation.transformFunction.apply(clone(postFilter)).eachWithIndex { instance, fileIndex ->
                    final Path subfolder = individualIterationPath.resolve('subfolder' + fileIndex.intdiv(FILES_PER_FOLDER))
                    if (fileIndex % FILES_PER_FOLDER == 0) {
                        FileIOUtils.mkdirs(subfolder)
                    }
                    final File file = transformation.dicomFileWriter.writeDicom(instance, subfolder, fileIndex)
                    filesForOverallZip << file
                    filesForTransformation << file
                }
            }
            if (transformation.produceZip) {
                zip(locateZipForIndividualTransformation(transformation), filesForTransformation)
            }
        }
        if (produceOverallZip) {
            zip(locateOverallZip(), filesForOverallZip)
        }

       completionMarker.toFile() << 'This marker exists to show that DICOM data has been produced locally for a test. Please do not mess with the data in this directory if you wish to run the tests successfully.'
    }

    private List<DatasetWithFMI> readBaseData() {
        if (baseData) {
            baseData.download()
            final ZipFile zipFile = new ZipFile(baseData.toFile())
            zipFile.entries().toList().findResults { zipEntry ->
                !zipEntry.directory ? DicomUtils.readDicom(zipFile.getInputStream(zipEntry)) : null
            }
        } else {
            []
        }
    }

    private void zip(Path destinationZip, List<File> filesToZip) {
        final Path baseDir = baseLevelDir()
        Files.createFile(destinationZip)
        final ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(destinationZip.toFile()))

        filesToZip.each { file ->
            if (file.isFile() && !file.name.endsWith('.zip')) {
                zipOutputStream.putNextEntry(new ZipEntry(baseDir.relativize(file.toPath()).toString()))
                IOUtils.copy(new FileInputStream(file), zipOutputStream)
                zipOutputStream.closeEntry()
            }
        }
        zipOutputStream.close()
    }

    private static List<DatasetWithFMI> clone(List<DatasetWithFMI> dicomInstances) {
        dicomInstances.collect { instance ->
            DicomUtils.clone(instance)
        }
    }

}

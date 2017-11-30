package org.nrg.testing.xnat.processing.files;

import org.nrg.testing.file.FileIO;
import org.nrg.testing.xnat.processing.files.comparators.FileComparator;

import java.io.File;

public class PipelineFile {

    String path;
    String collection;
    FileComparator fileComparator;

    public PipelineFile(String path, String collection, FileComparator fileComparator) {
        this.path = path;
        this.collection = collection;
        this.fileComparator = fileComparator;
    }

    public String checkComparatorSatisfied(String pathToFiles) {
        if (fileComparator == null) return null;
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        File file = FileIO.recursiveFind(new File(pathToFiles), fileName);
        if (file == null) return "Could not find file " + fileName;
        try {
            file = fileComparator.mutateFile(file);
        } catch (Exception e) {
            return "Error in applying file mutator to " + fileName;
        }
        return fileComparator.checkFileMatches(file);
    }
}

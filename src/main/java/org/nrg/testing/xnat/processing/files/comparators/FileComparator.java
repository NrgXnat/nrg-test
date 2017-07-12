/*
 * FileComparator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.processing.files.comparators;

import org.nrg.testing.xnat.processing.files.mutators.FileMutator;

import java.io.File;

public abstract class FileComparator {

    protected FileMutator mutator;

    public File mutateFile(File file) {
        return (mutator == null) ? file : mutator.mutateFile(file);
    }

    public void addMutator(FileMutator mutator) {
        this.mutator = mutator;
    }

    public abstract String checkFileMatches(File file);

}

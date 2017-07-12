/*
 * FileMutator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.xnat.processing.files.mutators;

import java.io.File;

public abstract class FileMutator {

    public abstract File mutateFile(File file);

}

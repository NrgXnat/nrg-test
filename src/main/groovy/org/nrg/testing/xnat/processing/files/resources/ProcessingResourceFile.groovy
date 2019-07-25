package org.nrg.testing.xnat.processing.files.resources

import org.nrg.xnat.pogo.resources.ResourceFile

class ProcessingResourceFile extends ResourceFile {

    boolean regex = false
    String mutator
    String comparator
    String compareTo
    String expectedText
    long expectedSize

}

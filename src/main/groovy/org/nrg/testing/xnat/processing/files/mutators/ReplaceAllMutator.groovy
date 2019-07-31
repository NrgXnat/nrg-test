package org.nrg.testing.xnat.processing.files.mutators

class ReplaceAllMutator extends FileMutator {

    Map<String, String> replacements = [:]

    @Override
    File mutateFile(File file) {
        String fileContents = file.text
        replacements.each { key, value ->
            fileContents = fileContents.replaceAll(key, value)
        }
        file.text = fileContents
        file
    }

}

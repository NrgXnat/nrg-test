package org.nrg.testing.xnat.processing.files.mutators

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes([
        @JsonSubTypes.Type(value = ReplaceAllMutator, name = 'replaceAll'),
        @JsonSubTypes.Type(value = DecompressGzipMutator, name = 'ungzip')
])
abstract class FileMutator {

    abstract File mutateFile(File file)

}

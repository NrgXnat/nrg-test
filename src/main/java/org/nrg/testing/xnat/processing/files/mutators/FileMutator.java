package org.nrg.testing.xnat.processing.files.mutators;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.File;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReplaceAllMutator.class, name = "replaceAll"),
        @JsonSubTypes.Type(value = DecompressGzipMutator.class, name = "ungzip")
})
public abstract class FileMutator {

    public abstract File mutateFile(File file);

}

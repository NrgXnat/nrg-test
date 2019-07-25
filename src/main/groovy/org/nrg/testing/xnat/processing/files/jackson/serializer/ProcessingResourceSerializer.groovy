package org.nrg.testing.xnat.processing.files.jackson.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile
import org.nrg.xnat.jackson.serializers.CustomSerializer

class ProcessingResourceSerializer extends CustomSerializer<ProcessingResource> {

    @Override
    void serialize(ProcessingResource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject()

        final List<ProcessingResourceFile> simpleFiles, complexFiles
        (simpleFiles, complexFiles) = value.processingFiles().split { file ->
            file.comparator == null
        }

        writeStringFieldIfNonnull(gen, 'folder', value.folder)
        writeBooleanFieldIfTrue(gen, 'regex', value.isRegex)
        writeStringFieldIfNonnull(gen, 'secondaryResources', value.secondaryResources)
        writeListFieldIfNonempty(gen, 'files', simpleFiles*.fullPath())
        writeListFieldIfNonempty(gen, 'complexFiles', complexFiles)

        gen.writeEndObject()
    }

}

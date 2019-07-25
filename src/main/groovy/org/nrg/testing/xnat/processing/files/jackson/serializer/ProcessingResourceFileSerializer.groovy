package org.nrg.testing.xnat.processing.files.jackson.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile
import org.nrg.xnat.jackson.serializers.CustomSerializer

class ProcessingResourceFileSerializer extends CustomSerializer<ProcessingResourceFile> {

    @Override
    void serialize(ProcessingResourceFile value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject()

        writeStringFieldIfNonnull(gen, 'name', value.fullPath())
        writeBooleanFieldIfTrue(gen, 'regex', value.regex)
        writeStringFieldIfNonnull(gen, 'compareTo', value.compareTo)
        writeStringFieldIfNonnull(gen, 'md5', value.md5)
        writeStringFieldIfNonnull(gen, 'expectedText', value.expectedText)
        writeStringFieldIfNonnull(gen, 'comparator', value.comparator)
        writeStringFieldIfNonnull(gen, 'mutator', value.mutator)

        gen.writeEndObject()
    }

}

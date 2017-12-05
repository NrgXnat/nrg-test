package org.nrg.testing.xnat.processing.files.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;
import org.nrg.xnat.jackson.serializers.CustomSerializer;

import java.io.IOException;

public class ProcessingResourceFileSerializer extends CustomSerializer<ProcessingResourceFile> {

    @Override
    public void serialize(ProcessingResourceFile value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        writeStringFieldIfNonnull(gen, "name", value.fullPath());
        writeBooleanFieldIfTrue(gen, "regex", value.isRegex());
        writeStringFieldIfNonnull(gen, "compareTo", value.getCompareTo());
        writeStringFieldIfNonnull(gen, "md5", value.getMd5());
        writeStringFieldIfNonnull(gen, "expectedText", value.getExpectedText());
        writeStringFieldIfNonnull(gen, "comparator", value.getComparator());
        writeStringFieldIfNonnull(gen, "mutator", value.getMutator());

        gen.writeEndObject();
    }

}

package org.nrg.testing.xnat.processing.files.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;
import org.nrg.xnat.jackson.serializers.CustomSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessingResourceSerializer extends CustomSerializer<ProcessingResource> {

    @Override
    public void serialize(ProcessingResource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        final List<String> simpleFiles = new ArrayList<>();
        final List<ProcessingResourceFile> complexFiles = new ArrayList<>();
        for (ProcessingResourceFile file : value.processingFiles()) {
            if (file.getComparator() == null) {
                simpleFiles.add(file.fullPath());
            } else {
                complexFiles.add(file);
            }
        }

        writeStringFieldIfNonnull(gen, "folder", value.getFolder());
        if (value.isRegex()) gen.writeBooleanField("regex", true);
        writeStringFieldIfNonnull(gen, "secondaryResources", value.getSecondaryResources());
        writeListFieldIfNonempty(gen, "files", simpleFiles);
        writeListFieldIfNonempty(gen, "complexFiles", complexFiles);

        gen.writeEndObject();
    }

}

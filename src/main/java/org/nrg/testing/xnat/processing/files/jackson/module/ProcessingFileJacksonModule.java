package org.nrg.testing.xnat.processing.files.jackson.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.nrg.testing.xnat.processing.files.jackson.deserializer.ProcessingResourceDeserializer;
import org.nrg.testing.xnat.processing.files.jackson.serializer.ProcessingResourceFileSerializer;
import org.nrg.testing.xnat.processing.files.jackson.serializer.ProcessingResourceSerializer;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;

public class ProcessingFileJacksonModule {

    public static SimpleModule build() {
        final SimpleModule module = new SimpleModule("File Processing Validation (De)serializers");

        module.addDeserializer(ProcessingResource.class, new ProcessingResourceDeserializer());
        module.addSerializer(ProcessingResource.class, new ProcessingResourceSerializer());
        module.addSerializer(ProcessingResourceFile.class, new ProcessingResourceFileSerializer());

        return module;
    }

}


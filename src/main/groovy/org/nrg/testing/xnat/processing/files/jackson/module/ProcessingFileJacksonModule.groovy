package org.nrg.testing.xnat.processing.files.jackson.module

import com.fasterxml.jackson.databind.module.SimpleModule
import org.nrg.testing.xnat.processing.files.jackson.deserializer.ProcessingResourceDeserializer
import org.nrg.testing.xnat.processing.files.jackson.serializer.ProcessingResourceFileSerializer
import org.nrg.testing.xnat.processing.files.jackson.serializer.ProcessingResourceSerializer
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile

class ProcessingFileJacksonModule extends SimpleModule {
    
    ProcessingFileJacksonModule() {
        super('File Processing Validation (De)serializers')
        addDeserializer(ProcessingResource, new ProcessingResourceDeserializer())
        addSerializer(ProcessingResource, new ProcessingResourceSerializer())
        addSerializer(ProcessingResourceFile, new ProcessingResourceFileSerializer())
    }

}


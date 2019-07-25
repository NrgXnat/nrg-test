package org.nrg.testing.xnat.processing.files.jackson.deserializer

import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile
import org.nrg.xnat.jackson.deserializers.CustomDeserializer

class ProcessingResourceDeserializer extends CustomDeserializer<ProcessingResource> {

    @SuppressWarnings('ChangeToOperator')
    @Override
    ProcessingResource deserialize(ObjectCodec objectCodec, JsonNode jsonNode) throws IOException {
        final ProcessingResource processingResource = new ProcessingResource()

        processingResource.setFolder(jsonNode.get('folder').asText())
        if (jsonNode.has('regex') && jsonNode.get('regex').asBoolean()) {
            processingResource.setIsRegex(true)
        }
        if (jsonNode.has('secondaryResources')) {
            processingResource.setSecondaryResources(jsonNode.get('secondaryResources').asText())
        }
        if (jsonNode.has('complexFiles')) {
            processingResource.resourceFiles.addAll(readObjectList(jsonNode, 'complexFiles', objectCodec, ProcessingResourceFile))
        }
        if (jsonNode.has('files')) {
            jsonNode.get('files').elements().each { node ->
                processingResource.resourceFiles << new ProcessingResourceFile().name((node as TextNode).asText())
            }
        }

        processingResource
    }

}

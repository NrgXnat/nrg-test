package org.nrg.testing.xnat.processing.files.jackson.deserializer;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResource;
import org.nrg.testing.xnat.processing.files.resources.ProcessingResourceFile;
import org.nrg.xnat.jackson.deserializers.CustomDeserializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ProcessingResourceDeserializer extends CustomDeserializer<ProcessingResource> {

    @Override
    public ProcessingResource deserialize(ObjectCodec objectCodec, JsonNode jsonNode) throws IOException {
        final ProcessingResource processingResource = new ProcessingResource();

        processingResource.setFolder(jsonNode.get("folder").asText());
        if (jsonNode.has("regex") && jsonNode.get("regex").asBoolean()) processingResource.setRegex(true);
        if (jsonNode.has("secondaryResources")) processingResource.setSecondaryResources(jsonNode.get("secondaryResources").asText());
        if (jsonNode.has("complexFiles")) {
            final List<ProcessingResourceFile> subclassList = readObjectList(jsonNode, "complexFiles", objectCodec, ProcessingResourceFile.class);
            for (ProcessingResourceFile file : subclassList) {
                processingResource.getResourceFiles().add(file);
            }
        }
        if (jsonNode.has("files")) {
            for (Iterator<JsonNode> nodeIterator = jsonNode.get("files").elements(); nodeIterator.hasNext();) {
                final TextNode fileNode = (TextNode) nodeIterator.next();
                processingResource.getResourceFiles().add(new ProcessingResourceFile().name(fileNode.asText()));
            }
        }

        return processingResource;
    }

}

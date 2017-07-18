package org.nrg.testing.xnat.extensions;

import org.nrg.xnat.Extension;
import org.nrg.xnat.pojo.resources.ResourceFile;

import java.io.File;

public class ResourceFileExtension implements Extension<ResourceFile> {

    private File file;

    public ResourceFileExtension(ResourceFile resourceFile, File javaFile) {
        resourceFile.setExtension(this);
        file = javaFile;
    }

    public File getFile() {
        return file;
    }

}

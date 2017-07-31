package org.nrg.testing.xnat.extensions;

import org.nrg.xnat.pojo.extensions.ResourceFileExtension;
import org.nrg.xnat.pojo.resources.ResourceFile;

import java.io.File;

public class SimpleResourceFileExtension extends ResourceFileExtension {

    private File file;

    public SimpleResourceFileExtension(ResourceFile resourceFile, File file) {
        super(resourceFile);
        this.file = file;
    }

    public SimpleResourceFileExtension(File file) {
        this(null, file);
    }

    @Override
    public File getJavaFile() {
        return file;
    }

}

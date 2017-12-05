package org.nrg.testing.xnat.processing.files.resources;

import org.nrg.xnat.pogo.resources.Resource;

public class GenericResource extends Resource {

    private String url;

    public GenericResource(String url) {
        this.url = url;
    }

    @Override
    public String resourceUrl() {
        return url;
    }

}

package org.nrg.testing.xnat.processing.files.resources

import org.nrg.xnat.pogo.resources.Resource

class GenericResource extends Resource {

    String url

    GenericResource() {

    }

    GenericResource(String url) {
        this.url = url
    }

    @Override
    String resourceUrl() {
        url
    }

}

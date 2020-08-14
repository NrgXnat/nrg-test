package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_7dev)
class Xnat_1_8_0 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.8.0']
    }

}

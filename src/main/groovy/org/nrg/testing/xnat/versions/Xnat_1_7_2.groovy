package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_6dev)
class Xnat_1_7_2 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.7.2']
    }

}

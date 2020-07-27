package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_7_6)
class Xnat_1_7_7 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.7.7']
    }

}

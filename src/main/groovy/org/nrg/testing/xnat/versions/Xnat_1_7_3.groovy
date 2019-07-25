package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_7_2)
class Xnat_1_7_3 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.7.3', '1.7.3.1']
    }

}

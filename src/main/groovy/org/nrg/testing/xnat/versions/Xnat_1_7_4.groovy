package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_7_3)
class Xnat_1_7_4 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.7.4', '1.7.4.1']
    }

}

package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_7_5)
class Xnat_1_7_5_2 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.7.5.2', '1.7.5.3']
    }

}

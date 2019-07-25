package org.nrg.testing.xnat.versions

import org.nrg.testing.annotations.Follows

@Follows(Xnat_1_7_4)
class Xnat_1_7_5 extends XnatVersion {

    @Override
    List<String> getVersionKeys() {
        ['1.7.5', '1.7.5.1']
    }

}

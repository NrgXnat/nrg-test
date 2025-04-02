package org.nrg.testing.xnat.rest

import org.nrg.xnat.versions.*

class XnatRestDriver_1_7 extends XnatRestDriver {

    @Override
    List<Class<? extends XnatVersion>> getHandledVersions() {
        XnatVersionList.knownVersionsAfter(Xnat_1_7_2)
    }

}

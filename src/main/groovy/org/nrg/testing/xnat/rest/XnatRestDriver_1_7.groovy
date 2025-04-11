package org.nrg.testing.xnat.rest

import org.nrg.xnat.versions.Xnat_1_7_2
import org.nrg.xnat.versions.XnatVersion
import org.nrg.xnat.versions.XnatVersionList

class XnatRestDriver_1_7 extends XnatRestDriver {

    @Override
    List<Class<? extends XnatVersion>> getHandledVersions() {
        XnatVersionList.knownVersionsAfter(Xnat_1_7_2)
    }

}

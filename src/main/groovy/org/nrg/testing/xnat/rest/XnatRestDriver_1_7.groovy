package org.nrg.testing.xnat.rest

import org.nrg.testing.xnat.versions.*

class XnatRestDriver_1_7 extends XnatRestDriver {

    @Override
    List<Class<? extends XnatVersion>> getHandledVersions() {
        [Xnat_1_7_3, Xnat_1_7_4, Xnat_1_7_5, Xnat_1_7_5_2, Xnat_1_7_6, Xnat_1_7dev]
    }

}

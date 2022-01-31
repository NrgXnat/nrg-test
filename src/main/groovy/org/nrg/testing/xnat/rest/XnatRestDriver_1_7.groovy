package org.nrg.testing.xnat.rest

import org.nrg.xnat.versions.*

class XnatRestDriver_1_7 extends XnatRestDriver {

    @Override
    List<Class<? extends XnatVersion>> getHandledVersions() {
        [Xnat_1_7_3, Xnat_1_7_4, Xnat_1_7_5, Xnat_1_7_5_2, Xnat_1_7_6, Xnat_1_7_7, Xnat_1_8_0, Xnat_1_8_1, Xnat_1_8_2, Xnat_1_8_2_2, Xnat_1_8_3, Xnat_1_8_4, Xnat_1_8_5]
    }

}

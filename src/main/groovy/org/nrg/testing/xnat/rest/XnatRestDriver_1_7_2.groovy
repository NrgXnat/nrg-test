package org.nrg.testing.xnat.rest

import org.nrg.testing.util.ResourceLoader
import org.nrg.testing.xnat.XnatObjectUtils
import org.nrg.testing.xnat.versions.XnatVersion
import org.nrg.testing.xnat.versions.Xnat_1_7_2
import org.nrg.xnat.enums.DicomEditVersion
import org.nrg.xnat.pogo.AnonScript

class XnatRestDriver_1_7_2 extends XnatRestDriver_1_7 {

    @Override
    List<Class<? extends XnatVersion>> getHandledVersions() {
        [Xnat_1_7_2]
    }

    @Override
    AnonScript getDefaultXnatAnonScript() {
        XnatObjectUtils.anonScriptFromFile(DicomEditVersion.DE_4, ResourceLoader.copyAndGetResource('1_7_2_default_anon.das'))
    }

}

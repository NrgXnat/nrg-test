package org.nrg.testing.xnat.rest;

import org.nrg.testing.util.ResourceLoader;
import org.nrg.testing.xnat.XnatObjectUtils;
import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.Xnat_1_7_2;
import org.nrg.xnat.enums.DicomEditVersion;
import org.nrg.xnat.pogo.AnonScript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XnatRestDriver_1_7_2 extends XnatRestDriver_1_7 {

    @Override
    public List<Class<? extends XnatVersion>> getHandledVersions() {
        return new ArrayList<Class<? extends XnatVersion>>(Collections.singletonList(Xnat_1_7_2.class));
    }

    @Override
    public AnonScript getDefaultXnatAnonScript() {
        return XnatObjectUtils.anonScriptFromFile(DicomEditVersion.DE_4, ResourceLoader.copyAndGetResource("1_7_2_default_anon.das"));
    }

}

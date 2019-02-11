package org.nrg.testing.xnat.rest;

import org.nrg.testing.xnat.versions.*;

import java.util.Arrays;
import java.util.List;

public class XnatRestDriver_1_7 extends XnatRestDriver {

    @Override
    public List<Class<? extends XnatVersion>> getHandledVersions() {
        return Arrays.asList(Xnat_1_7_3.class, Xnat_1_7_4.class, Xnat_1_7_5.class, Xnat_1_7_5_2.class, Xnat_1_7_6.class, Xnat_1_7dev.class);
    }

}

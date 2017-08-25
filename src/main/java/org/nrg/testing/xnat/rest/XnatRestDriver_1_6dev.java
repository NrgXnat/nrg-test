package org.nrg.testing.xnat.rest;

import org.nrg.testing.xnat.versions.XnatVersion;
import org.nrg.testing.xnat.versions.Xnat_1_6dev;
import org.nrg.xnat.pogo.users.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XnatRestDriver_1_6dev extends XnatRestDriver {

    @Override
    public List<Class<? extends XnatVersion>> getHandledVersions() {
        return new ArrayList<Class<? extends XnatVersion>>(Collections.singletonList(Xnat_1_6dev.class));
    }

    @Override
    public String getBuildInfo() {
        LOGGER.info("Build info REST call not available in XNAT 1.6.");
        return null;
    }

    @Override
    public void initializeXnat() {
        LOGGER.info("No XNAT initialization method available in 1.6");
    }

    @Override
    public void createUser(User user) {
        LOGGER.info("No user creation method available in 1.6");
    }

}

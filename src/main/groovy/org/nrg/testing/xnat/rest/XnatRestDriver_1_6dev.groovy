package org.nrg.testing.xnat.rest

import groovy.util.logging.Log4j
import org.nrg.testing.util.ResourceLoader
import org.nrg.testing.xnat.XnatObjectUtils
import org.nrg.testing.xnat.versions.XnatVersion
import org.nrg.testing.xnat.versions.Xnat_1_6dev
import org.nrg.xnat.enums.DicomEditVersion
import org.nrg.xnat.pogo.AnonScript
import org.nrg.xnat.pogo.users.User

@Log4j
class XnatRestDriver_1_6dev extends XnatRestDriver_1_7_2 {

    @Override
    List<Class<? extends XnatVersion>> getHandledVersions() {
        [Xnat_1_6dev.class]
    }

    @Override
    String getBuildInfo() {
        log.info('Build info REST call not available in XNAT 1.6.')
        null
    }

    @Override
    AnonScript getDefaultXnatAnonScript() {
        XnatObjectUtils.anonScriptFromFile(DicomEditVersion.DE_4, ResourceLoader.copyAndGetResource('1_6_default_anon.das'))
    }

    @Override
    void initializeXnat() {
        log.info('No XNAT initialization method available in 1.6')
    }

    @Override
    void createUser(User user) {
        log.info('No user creation method available in 1.6')
    }

}

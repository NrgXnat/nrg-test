package org.nrg.testing.tests

import org.nrg.testing.xnat.parallel.ServerStateGuard
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertNotNull
import static org.testng.AssertJUnit.assertNull

class ServerStateGuardTest {

    private static final List<String> SHARED_USERS = ['unitTest', 'admin']

    @Test
    void flagsSiteConfigWrites() {
        assertNotNull(reason('POST', '/xapi/siteConfig'))
        assertNotNull(reason('POST', '/xapi/siteConfig/requireLogin'))
        assertNull(reason('GET', '/xapi/siteConfig'))
        assertNull(reason('GET', '/xapi/siteConfig/buildInfo'))
    }

    @Test
    void flagsSiteLevelConfigServiceWrites() {
        assertNotNull(reason('PUT', '/data/config/edit/image/dicom/script'))  // site anon script
        assertNotNull(reason('PUT', '/data/config/dicom/projectRules'))       // DICOM routing rules
        assertNull(reason('PUT', '/data/config/edit/projects/PROJ1/image/dicom/script')) // project anon script
        assertNull(reason('GET', '/data/config/edit/image/dicom/script'))
    }

    @Test
    void flagsSharedUserMutations() {
        assertNotNull(reason('PUT', '/xapi/users/unitTest/roles'))
        assertNotNull(reason('DELETE', '/data/user/admin/sessions'))
        assertNull(reason('PUT', '/xapi/users/aB3xY9zQ2wK4mP/roles')) // a class-private random user
        assertNull(reason('POST', '/xapi/users'))                     // user creation is additive
        assertNull(reason('GET', '/xapi/users/unitTest/roles'))
    }

    @Test
    void allowsOrdinaryProjectTraffic() {
        assertNull(reason('POST', '/data/projects'))
        assertNull(reason('PUT', '/data/projects/PROJ1/subjects/SUBJ1'))
        assertNull(reason('DELETE', '/data/projects/PROJ1'))
        assertNull(reason('POST', '/data/services/import'))
    }

    private static String reason(String method, String path) {
        ServerStateGuard.mutationReason(method, path, SHARED_USERS)
    }

}

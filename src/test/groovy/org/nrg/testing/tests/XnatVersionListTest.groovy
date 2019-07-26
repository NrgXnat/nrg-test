package org.nrg.testing.tests

import org.nrg.testing.xnat.rest.XnatRestDriver_1_6dev
import org.nrg.testing.xnat.rest.XnatRestDriver_1_7
import org.nrg.testing.xnat.rest.XnatRestDriver_1_7_2
import org.nrg.testing.xnat.versions.XnatVersionList
import org.nrg.testing.xnat.versions.Xnat_1_6dev
import org.nrg.testing.xnat.versions.Xnat_1_7_2
import org.nrg.testing.xnat.versions.Xnat_1_7_3
import org.testng.annotations.Test

import static org.testng.AssertJUnit.*

class XnatVersionListTest {

    @Test
    void testXnatVersionLineage() {
        XnatVersionList.readXnatVersions([Xnat_1_6dev, Xnat_1_7_2, Xnat_1_7_3], [XnatRestDriver_1_6dev, XnatRestDriver_1_7_2, XnatRestDriver_1_7])
        assertTrue(XnatVersionList.firstFollowsSecond(Xnat_1_7_3, Xnat_1_7_2))
        assertTrue(XnatVersionList.firstFollowsSecond(Xnat_1_7_3, Xnat_1_6dev))
        assertTrue(XnatVersionList.firstFollowsSecond(Xnat_1_7_2, Xnat_1_6dev))
        assertFalse(XnatVersionList.firstFollowsSecond(Xnat_1_7_3, Xnat_1_7_3))
        assertFalse(XnatVersionList.firstFollowsSecond(Xnat_1_7_2, Xnat_1_7_2))
        assertFalse(XnatVersionList.firstFollowsSecond(Xnat_1_7_2, Xnat_1_7_3))
        assertFalse(XnatVersionList.firstFollowsSecond(Xnat_1_6dev, Xnat_1_7_3))
    }

}

package org.nrg.testing.tests

import org.nrg.testing.util.VersionParser
import org.testng.annotations.Test

import static org.testng.AssertJUnit.assertFalse
import static org.testng.AssertJUnit.assertTrue

class VersionParserTest {

    @Test
    void testPluginVersionParsing() {
        assertTrue(VersionParser.versionCompatible('1.0', '1.0'))
        assertTrue(VersionParser.versionCompatible('1.0', '1.0.0'))
        assertTrue(VersionParser.versionCompatible('1.0', '1.0.1'))
        assertTrue(VersionParser.versionCompatible('1.0', '1.0.1-SNAPSHOT'))
        assertTrue(VersionParser.versionCompatible('1.0', '1.1-BETA'))
        assertFalse(VersionParser.versionCompatible('1.4', '1.3.8'))
        assertFalse(VersionParser.versionCompatible('1.4', '1.2-RC'))
        assertFalse(VersionParser.versionCompatible('2.0', '1.9.8'))
    }

}

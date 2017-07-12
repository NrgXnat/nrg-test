package org.nrg.testing.tests;

import org.nrg.testing.UnitTestUtils;
import org.nrg.testing.file.FileIO;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class MD5Test {

    @Test
    public void testMD5() {
        final String CORRECT_HASH = "68a24bab028d4c2e2fe2c962e16f2f02";
        try {
            String hash = FileIO.calculateMD5(Paths.get(UnitTestUtils.DATA_LOCATION, "imuta.png").toFile());
            if (!hash.equals(CORRECT_HASH)) {
                Assert.fail(String.format("Calculated checksum of %s doesn't match expected checksum: %s", hash, CORRECT_HASH));
            }
        } catch (IOException ioe) {
            Assert.fail("Failed to calculate MD5 hash: " + ioe);
        }
    }
}

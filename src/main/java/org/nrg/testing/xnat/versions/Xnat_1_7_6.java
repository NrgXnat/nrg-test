package org.nrg.testing.xnat.versions;

import org.nrg.testing.annotations.Follows;

@Follows(Xnat_1_7_5_2.class)
public class Xnat_1_7_6 extends XnatVersion {

    @Override
    public String getVersionKey() {
        return "1.7.6";
    }

}

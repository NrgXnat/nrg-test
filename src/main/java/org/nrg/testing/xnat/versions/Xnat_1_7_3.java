package org.nrg.testing.xnat.versions;

import org.nrg.testing.annotations.Follows;

@Follows(Xnat_1_7_2.class)
public class Xnat_1_7_3 extends XnatVersion {

    @Override
    public String getVersionKey() {
        return "1.7.3";
    }

}

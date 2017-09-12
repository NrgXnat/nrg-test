package org.nrg.testing.xnat.versions;

import org.nrg.testing.annotations.Follows;

@Follows(Xnat_1_7_3.class)
public class Xnat_1_7_4 extends XnatVersion {

    @Override
    public String getVersionKey() {
        return "1.7.4";
    }

}

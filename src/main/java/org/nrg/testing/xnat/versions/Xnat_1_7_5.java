package org.nrg.testing.xnat.versions;

import org.nrg.testing.annotations.Follows;

@Follows(Xnat_1_7_4.class)
public class Xnat_1_7_5 extends XnatVersion {

    @Override
    public String getVersionKey() {
        return "1.7.5";
    }

}

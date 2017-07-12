package org.nrg.testing.xnat;

public class XnatAliasToken {

    private String alias;
    private String secret;

    public XnatAliasToken(String alias, String secret) {
        this.alias = alias;
        this.secret = secret;
    }

    public String getAlias() {
        return alias;
    }

    public String getSecret() {
        return secret;
    }

}

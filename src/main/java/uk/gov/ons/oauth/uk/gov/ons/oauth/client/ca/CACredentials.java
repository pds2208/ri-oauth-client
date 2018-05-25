package uk.gov.ons.oauth.uk.gov.ons.oauth.client.ca;

import uk.gov.ons.oauth.uk.gov.ons.oauth.client.OAuthCredentials;

public class CACredentials extends OAuthCredentials {

    private String tokenType;
    private String scope;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}

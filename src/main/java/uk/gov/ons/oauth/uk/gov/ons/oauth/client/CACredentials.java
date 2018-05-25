package uk.gov.ons.oauth.uk.gov.ons.oauth.client;

public class CACredentials {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean tokenIsValid() {
        return true;
    }

}

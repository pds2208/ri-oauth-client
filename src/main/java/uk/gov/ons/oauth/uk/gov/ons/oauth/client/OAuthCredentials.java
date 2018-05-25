package uk.gov.ons.oauth.uk.gov.ons.oauth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class OAuthCredentials {
    private static Logger logger = LoggerFactory.getLogger(OAuthCredentials.class);
    private String token;
    private int expiry;
    private long expiryTime = 0;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiryTime() {
        return expiry;
    }

    public void setExpiryTime(int expiry) {
        this.expiry = expiry;
        this.expiryTime = new Date().getTime() / 1000 + expiry;
    }

    public boolean hasExpired() {
        boolean b = new Date().getTime() / 1000 >= expiryTime - 10; // give it 10 seconds grace
        if (b) {
            logger.debug("Token has expired");
        } else {
            logger.debug("Token is valid");
        }
        return b;
    }

}

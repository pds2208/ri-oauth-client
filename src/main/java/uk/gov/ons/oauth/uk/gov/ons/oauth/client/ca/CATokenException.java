package uk.gov.ons.oauth.uk.gov.ons.oauth.client.ca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CATokenException extends RuntimeException {
    private static Logger logger = LoggerFactory.getLogger(CAOAuthClient.class);

    public CATokenException(String message) {
        super(message);
        logger.error("CA Token Exception {}", message);
    }

}

package uk.gov.ons.oauth.uk.gov.ons.oauth.client.ca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.oauth.uk.gov.ons.oauth.client.OAuthClient;
import uk.gov.ons.oauth.uk.gov.ons.oauth.client.OAuthOptions;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.net.*;
import java.util.stream.Collectors;

public class CAOAuthClient extends OAuthClient {

    private OAuthOptions options;
    private CACredentials credentials;

    private static Logger logger = LoggerFactory.getLogger(CAOAuthClient.class);

    public CAOAuthClient(OAuthOptions options, CACredentials caCredentials) {
        this.options = options;
        this.credentials = caCredentials;
    }

    @Override
    public OAuthOptions getOptions() {
        return options;
    }

    public String callService() {

        try {
            if (credentials.hasExpired()) {
                getToken();
            }
        } catch (CATokenException e) {
            return null;
        }

        try {
            URL url = new URL(options.getServiceEndpoint());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("Authorization", "Bearer " + credentials.getToken());

            con.setConnectTimeout(options.getConnectionTimeout());
            con.setReadTimeout(options.getReadTimeout());

            int status = con.getResponseCode();
            logger.debug("Status from service call: {}", status);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                return  in.lines().collect(Collectors.joining());
            }

        } catch (ProtocolException e) {
            logger.error("Received protocol exception on creating URL {}", e.getMessage());
            return null;
        } catch (MalformedURLException e) {
            logger.error("URL is malformed - check configuration {}", e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("Received I/O exception on opening URL. Is the service available? {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void parseJSON(String jsonString) {

        StringReader reader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(reader);

        JsonObject newJsonObject = jsonReader.readObject();

        String accessToken = newJsonObject.getString("access_token", "");
        logger.info("Access Token: {}", accessToken);
        credentials.setToken(accessToken);

        String tokenType = newJsonObject.getString("token_type", "");
        logger.debug("Token Type: {}", tokenType);
        credentials.setTokenType(tokenType);

        int expiry = newJsonObject.getInt("expires_in", 0);
        logger.debug("Expires in: {} seconds", expiry);
        credentials.setExpiryTime(expiry);

        String scope = newJsonObject.getString("scope", "");
        logger.debug("scope: {}", scope);
        credentials.setScope(scope);
    }
}

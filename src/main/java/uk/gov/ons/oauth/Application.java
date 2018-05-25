package uk.gov.ons.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ons.oauth.uk.gov.ons.oauth.client.CACredentials;
import uk.gov.ons.oauth.uk.gov.ons.oauth.client.CAOAuthClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.lang.System.exit;

public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        // Needed for testing on localhost
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) ->
                hostname.equals("localhost"));

        Application a = new Application();

        a.start();
    }

    private void start() {
        CAOAuthClient oa = new CAOAuthClient();

        Properties p = loadProperties();
        String clientId = p.getProperty("oauth.client.id");
        String clientSecret = p.getProperty("oauth.client.secret");
        String scope = p.getProperty("oauth.client.scope");
        String tokenEndpoint = p.getProperty("oauth.server.token");
        String serviceEndpoint = p.getProperty("oauth.server.endpoint");
        String connectionTimeout = p.getProperty("oauth.client.connectionTimeout", "5000");
        String readTimeout = p.getProperty("oauth.client.readTimeout", "5000");

        oa.setClientId(clientId);
        oa.setClientSecret(clientSecret);
        oa.setTokenEndpoint(tokenEndpoint);
        oa.setServiceEndpoint(serviceEndpoint);
        oa.setReadTimeout(Integer.parseInt(readTimeout));
        oa.setConnectTimeout(Integer.parseInt(connectionTimeout));
        oa.setScope(scope);

        logger.debug(oa.callService());

        exit(0);
    }

    private Properties loadProperties() {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "application.properties";

        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            logger.error("Cannot load application.properties file");
            exit(1);
        }
        return appProps;
    }
}

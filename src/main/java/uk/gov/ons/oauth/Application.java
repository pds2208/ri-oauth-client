package uk.gov.ons.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        OAuth oa = new OAuth();

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

        if (scope != null) {
            List<String> sc = Stream.of(scope.split(","))
                    .map (String::trim)
                    .collect(Collectors.toList());
            oa.setScope(sc);
        } else {
            oa.setScope(null);
        }

        Map<String, String> response = oa.getToken();
        if (response != null) {
            logger.debug(oa.callService(response.get("access_token")));
        }
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

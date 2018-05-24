package uk.gov.ons.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OAuth {

    private String clientId;
    private String clientSecret;
    private String scope;
    private String tokenEndpoint;
    private String serviceEndpoint;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;

    private static Logger logger = LoggerFactory.getLogger(OAuth.class);

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String callService(String token) {

        try {
            URL url = new URL(serviceEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + token);

            con.setConnectTimeout(connectTimeout);
            con.setReadTimeout(readTimeout);

            int status = con.getResponseCode();
            logger.debug("Status from REST call: {}", status);
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
            logger.error("Received I/O exception on opening URL. IOs the service available? {}", e.getMessage());
            return null;
        }

    }

    public Map<String, String> getToken() {

        assert (clientId != null);
        assert (clientSecret != null);
        assert (scope != null);

        HttpURLConnection con;

        try {
            URL url = new URL(tokenEndpoint);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("grant_type", "client_credentials");
            parameters.put("client_id", clientId);
            parameters.put("client_secret", clientSecret);

            if (scope != null && scope.length != 0) {
                parameters.put("scope", scope);
            }

            con.setDoOutput(true);

            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                out.writeBytes(getParamsString(parameters));
                out.flush();
                con.setConnectTimeout(connectTimeout);
                con.setReadTimeout(readTimeout);
            } catch (IOException e) {
                logger.error("Received I/O exception on creating output stream {}", e.getMessage());
                return null;
            }

            int status = con.getResponseCode();
            if (status != 200) {
                logger.error("Received status {}, status 200 expected", status);
                return null;
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                return parseJSON(content.toString());
            }

        } catch (IOException e) {
            logger.error("Received I/O on creating connection or opening stream {}", e.getMessage());
            return null;
        }

    }

    private static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    private Map<String, String> parseJSON(String jsonString) {

        Map<String, String> response = new HashMap<>();

        StringReader reader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(reader);

        JsonObject newJsonObject = jsonReader.readObject();

        String accessToken = newJsonObject.getString("access_token", "");
        logger.info("Access Token: {}", accessToken);
        response.put("access_token", accessToken);

        String tokenType = newJsonObject.getString("token_type", "");
        logger.debug("Token Type: {}", tokenType);
        response.put("token_type", tokenType);

        int expires = newJsonObject.getInt("expires_in", 0);
        logger.debug("Expires in: {}", expires);
        response.put("expires_in", "" + expires);

        String scope = newJsonObject.getString("scope", "");
        logger.debug("scope: {}", scope);
        response.put("scope", scope);

        return response;
    }
}

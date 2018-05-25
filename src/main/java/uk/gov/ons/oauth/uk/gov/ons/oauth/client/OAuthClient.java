package uk.gov.ons.oauth.uk.gov.ons.oauth.client;

import uk.gov.ons.oauth.uk.gov.ons.oauth.client.ca.CATokenException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class OAuthClient  {

    public abstract void parseJSON(String jsonString);
    public abstract OAuthOptions getOptions();

    protected void getToken() {

        try {
            HttpURLConnection con;
            String id = Optional.of(getOptions().getClientId()).filter(s -> !s.isEmpty())
                    .orElseThrow(() -> new CATokenException("Client ID has not been set"));

            String secret = Optional.of(getOptions().getClientSecret()).filter(s -> !s.isEmpty())
                    .orElseThrow(() -> new CATokenException("Client Secret has not been set"));

            URL url = new URL(getOptions().getTokenEndpoint());
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("grant_type", "client_credentials");
            parameters.put("client_id", id);
            parameters.put("client_secret", secret);

            if ((getOptions().getScope() != null) && (getOptions().getScope().length() != 0)) {
                parameters.put("scope", getOptions().getScope());
            }

            con.setDoOutput(true);

            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                out.writeBytes(getParamsString(parameters));
                out.flush();
                con.setConnectTimeout(getOptions().getConnectionTimeout());
                con.setReadTimeout(getOptions().getReadTimeout());
            } catch (IOException e) {
                throw new CATokenException("Received I/O exception on creating output stream  " + e.getMessage());
            }

            int status = con.getResponseCode();
            if (status != 200) {
                throw new CATokenException(String.format("Received status %s, status 200 expected", status));
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                parseJSON(content.toString());
            }

        } catch (IOException e) {
            throw new CATokenException("Received I/O on creating connection or opening stream " + e.getMessage());
        }

    }

    private String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
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

}

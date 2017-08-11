package com.justintime.utils;


import com.justintime.security.AuthorizationServerConfiguration;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class HttpRequest {

    private String server = "";
    private String clientCredentials = AuthorizationServerConfiguration.client + ":" + AuthorizationServerConfiguration.secret;
    private String encodedClient = Base64.getEncoder().encodeToString(clientCredentials.getBytes());
    private HttpURLConnection connection = null;
    private BufferedReader bufferedReader = null;

    public JSONObject openConnection(String uri, HttpServletRequest request, boolean isOurEndpoint) {

        server = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
        URL url;

        try {
            if (isOurEndpoint) url = new URL(server + uri);
            else url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + encodedClient);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != 200)
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return new JSONObject(bufferedReader.readLine());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null)
                connection.disconnect();
        }

        return null;
    }
}

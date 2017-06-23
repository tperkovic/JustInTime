package com.justintime.utils;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.justintime.security.AuthorizationServerConfiguration;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;

import static org.springframework.security.oauth2.common.util.OAuth2Utils.CLIENT_ID;

public class CheckTokenRequest {
    private String server = "";
    private String clientCredentials = AuthorizationServerConfiguration.client + ":" + AuthorizationServerConfiguration.secret;
    private String encodedClient = Base64.getEncoder().encodeToString(clientCredentials.getBytes());
    private HttpURLConnection connection = null;
    private BufferedReader bufferedReader = null;
    private static final String  CLIENT_ID =  "892432124865-sbn3gr9incn89dit7t13jbnqv7qfhemp.apps.googleusercontent.com";

    public String getUsername(String accessToken, HttpServletRequest request) {

        server = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());

        try {
            URL url = new URL(server + "oauth/check_token?token=" + accessToken);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + encodedClient);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != 200)
                throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JSONObject jsonObject = new JSONObject(bufferedReader.readLine());
            return jsonObject.get("user_name").toString();

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

    public GoogleIdToken.Payload googleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String  googleClient = (String) payload.get("aud");

            if (emailVerified && googleClient.equals(CLIENT_ID)) return payload;
            else return null;

        } else {
            return null;
        }
    }
}

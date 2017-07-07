package com.justintime.utils;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.justintime.security.AuthorizationServerConfiguration;
import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class TokenRequest {
    private String server = "";
    private String clientCredentials = AuthorizationServerConfiguration.client + ":" + AuthorizationServerConfiguration.secret;
    private String encodedClient = Base64.getEncoder().encodeToString(clientCredentials.getBytes());
    private HttpURLConnection connection = null;
    private BufferedReader bufferedReader = null;
    private static final String  CLIENT_ID =  "892432124865-sbn3gr9incn89dit7t13jbnqv7qfhemp.apps.googleusercontent.com";

    public static final String CHECK_TOKEN_URL = "oauth/check_token?token=";
    public static final String GRANT_TYPE_PASSWORD = "oauth/token?grant_type=password&";

    public JSONObject endpoint(String endpointPath, HttpServletRequest request) {

        server = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());

        try {
            URL url = new URL(server + endpointPath);
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

    public OAuth2AccessToken accessToken(String username, String userRole, AuthorizationServerTokenServices tokenServices) {
        HashSet<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(userRole));

        HashMap<String, String> requestParameters = new HashMap<>();
        boolean approved = true;
        HashSet<String> scope = new HashSet<>();
        scope.add(AuthorizationServerConfiguration.scope);
        HashSet<String> resourceIds = new HashSet<>();
        HashSet<String> responseTypes = new HashSet<>();
        responseTypes.add("code");
        HashMap<String, Serializable> extensionProperties = new HashMap<>();

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, AuthorizationServerConfiguration.client,
                authorities, approved, scope, resourceIds, null, responseTypes, extensionProperties);


        User userPrincipal = new User(username, "", true, true, true, true, authorities);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);

        return tokenServices.createAccessToken(auth);
    }
}

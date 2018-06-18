package com.justintime.utils;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.justintime.security.AuthorizationServerConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class TokenRequest {
    private static final String GOOGLE_CLIENT_ID = "892432124865-sbn3gr9incn89dit7t13jbnqv7qfhemp.apps.googleusercontent.com";

    public GoogleIdToken.Payload googleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String googleClient = (String) payload.get("aud");

            if (emailVerified && googleClient.equals(GOOGLE_CLIENT_ID)) return payload;
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

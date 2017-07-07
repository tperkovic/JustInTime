package com.justintime.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.justintime.model.User;
import com.justintime.repository.UserRepository;
import com.justintime.utils.CustomUser;
import com.justintime.utils.NullAwareUtilsBean;
import com.justintime.utils.TokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthorizationServerEndpointsConfiguration configuration;

    private final SecureRandom random = new SecureRandom();

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    @RequestMapping(value = "/google/{idToken:.+}", method = RequestMethod.POST)
    public ResponseEntity<?> googleLogin(@PathVariable("idToken") String idToken) throws Exception {
        GoogleIdToken.Payload payload = new TokenRequest().googleIdToken(idToken);
        if (payload == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        String googleId = (String) payload.get("sub");

        User existingUser = userRepository.findBygoogleId(googleId);
        if (existingUser == null) {
            existingUser = userRepository.findBymail(payload.getEmail());
        }
        if (existingUser == null) {

            User user = new User();
            user.googleId = googleId;
            user.firstName = (String) payload.get("given_name");
            user.lastName = (String) payload.get("family_name");
            user.mail = payload.getEmail();
            user.setRole(ROLE_USER);
            String generatedPass = new BigInteger(130, random).toString(32);
            user.setPassword(passwordEncoder.encode(generatedPass));

            userRepository.save(user);

            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(ROLE_USER));
            inMemoryUserDetailsManager.createUser(new CustomUser(user.getMail(), user.getPassword(), authorities));

            existingUser = user;
        }

        if (existingUser.getGoogleId() == null && existingUser.getGoogleId().isEmpty()) {
            existingUser.setGoogleId(googleId);
            userRepository.save(existingUser);
        }

        OAuth2AccessToken accessToken =
                new TokenRequest().accessToken(existingUser.getMail(), existingUser.getRole(), configuration.getEndpointsConfigurer().getTokenServices());

        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<User> create(User user) throws Exception {
        User existingUser = userRepository.findBymail(user.getMail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLE_USER);
        if (existingUser != null && existingUser.getMail().equals(user.getMail())) {
            user.setId("User already exist!");
            return new ResponseEntity<>(user, HttpStatus.CONFLICT);
        }

        userRepository.save(user);

        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_USER));
        inMemoryUserDetailsManager.createUser(new CustomUser(user.getMail(), user.getPassword(), authorities));

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/read-all", method = RequestMethod.GET)
    public ResponseEntity<List<User>> readAll(){
        List<User> users = userRepository.findAll();
        if (users.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, User userParam) throws Exception {
        User user = userRepository.findByid(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (!(userParam.getPassword() == null))
            userParam.setPassword(passwordEncoder.encode(userParam.getPassword()));

        if (user.getRole() != null && user.getRole().equals(ROLE_ADMIN))
            userParam.setRole(ROLE_ADMIN);
        else userParam.setRole(ROLE_USER);

        NullAwareUtilsBean.CopyProperties(userParam, user);
        userRepository.save(user);

        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        inMemoryUserDetailsManager.updateUser(new CustomUser(user.getMail(), user.getPassword(), authorities));

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteUser(@PathVariable("id") String id){
        User user = userRepository.findByid(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        userRepository.delete(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@RequestParam("access_token") String accessToken, HttpServletRequest request) {
        String username = new TokenRequest().endpoint(TokenRequest.CHECK_TOKEN_URL + accessToken, request).get("user_name").toString();
        User user = userRepository.findBymail(username);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{mail:.+}", method = RequestMethod.GET)
    public ResponseEntity<User> getMail(@PathVariable("mail") String mail){
        User user = userRepository.findBymail(mail);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
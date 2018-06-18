package com.justintime.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.justintime.model.User;
import com.justintime.repository.UserRepository;
import com.justintime.utils.CustomUser;
import com.justintime.utils.NullAwareUtilsBean;
import com.justintime.utils.SocialUser;
import com.justintime.utils.TokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.social.FacebookAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

            User user = new SocialUser().createUser(googleId, null, (String) payload.get("given_name"), (String) payload.get("family_name"), payload.getEmail(), passwordEncoder);

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

    @RequestMapping(value = "/facebook/{token:.+}", method = RequestMethod.POST)
    public ResponseEntity<?> facebookLogin(@PathVariable("token") String token) throws Exception {
        Facebook facebook = new FacebookTemplate(token);
        String [] fields = {"id", "email", "first_name", "installed", "last_name", "verified"};
        org.springframework.social.facebook.api.User facebookUser = facebook.fetchObject("me", org.springframework.social.facebook.api.User.class, fields);
        if (facebookUser == null)
            return new ResponseEntity<>("Facebook user info not received!", HttpStatus.NOT_FOUND);

        if (!facebookUser.isVerified() || !facebookUser.isInstalled())
            return new ResponseEntity<>("Facebook user is not trusted!", HttpStatus.FORBIDDEN);

        User existingUser = userRepository.findByfacebookId(facebookUser.getId());
        if (existingUser == null) {
            existingUser = userRepository.findBymail(facebookUser.getEmail());
        }
        if (existingUser == null)
        {
            User user = new SocialUser().createUser(null, facebookUser.getId(), facebookUser.getFirstName(), facebookUser.getLastName(), facebookUser.getEmail(), passwordEncoder);

            userRepository.save(user);

            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(ROLE_USER));
            inMemoryUserDetailsManager.createUser(new CustomUser(user.getMail(), user.getPassword(), authorities));

            existingUser = user;
        }

        if (existingUser.getFacebookId() == null && existingUser.getFacebookId().isEmpty()) {
            existingUser.setFacebookId(facebookUser.getId());
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
    public ResponseEntity<List<User>> getAllUsers(){
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

        if (userParam.getPassword() != null)
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
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findBymail(username);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{mail:.+}", method = RequestMethod.GET)
    public ResponseEntity<User> getUserByMail(@PathVariable("mail") String mail){
        User user = userRepository.findBymail(mail);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
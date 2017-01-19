package com.justintime.controller;

import com.justintime.model.User;
import com.justintime.repository.UserRepository;
import com.justintime.security.AuthorizationServerConfiguration;
import com.justintime.utils.CustomUser;
import com.justintime.utils.NullAwareUtilsBean;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
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

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

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
        String username = "";
        String server = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
        String clientCredentials = AuthorizationServerConfiguration.client + ":" + AuthorizationServerConfiguration.secret;
        String encodedClient = Base64.getEncoder().encodeToString(clientCredentials.getBytes());
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

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
            username = jsonObject.get("user_name").toString();

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
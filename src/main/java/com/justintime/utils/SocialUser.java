package com.justintime.utils;

import com.justintime.controller.UserController;
import com.justintime.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SocialUser {

    private final SecureRandom random = new SecureRandom();

    public User createUser(String googleId, String facebookId, String firstName, String lastName, String email, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.googleId = googleId;
        user.facebookId = facebookId;
        user.firstName = firstName;
        user.lastName = lastName;
        user.mail = email;
        user.setRole(UserController.ROLE_USER);
        String generatedPass = new BigInteger(130, random).toString(32);
        user.setPassword(passwordEncoder.encode(generatedPass));

        return user;
    }
}

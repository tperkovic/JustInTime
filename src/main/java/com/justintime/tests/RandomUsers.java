package com.justintime.tests;


import com.justintime.controller.UserController;
import com.justintime.model.User;
import com.justintime.utils.HttpRequest;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;

public class RandomUsers {

    public HashMap<String, User> generate(int nUsers, HttpServletRequest httpServletRequest) {
        String randomUserAPI = "https://randomuser.me/api/";
        HashMap<String, User> generatedUsers = new HashMap<>();

        HttpRequest apiRequest = new HttpRequest();
        for (int i = 0; i < nUsers; i++) {
            User user = new User();
            ResponseEntity<?> response = apiRequest.openConnection(randomUserAPI, "GET", httpServletRequest, false);
            if (response == null) break;

            JSONObject result = new JSONObject(response.getBody().toString());
            result = (JSONObject) result.getJSONArray("results").get(0);
            user.firstName = result.getJSONObject("name").getString("first");
            user.lastName = result.getJSONObject("name").getString("last");
            user.mail = result.getString("email");
            user.setPassword(result.getJSONObject("login").getString("password"));
            user.setRole(UserController.ROLE_USER);
            generatedUsers.put(user.getMail(), user);
        }

        return generatedUsers;
    }
}

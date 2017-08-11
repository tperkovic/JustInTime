package com.justintime.tests;


import com.justintime.utils.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

public class RandomUsers {

    public ArrayList<String> generate(int nUsers, HttpServletRequest httpServletRequest) {
        String randomUserAPI = "https://randomuser.me/api/";
        ArrayList<String> generatedUsers = new ArrayList<>();

        HttpRequest apiRequest = new HttpRequest();
        for (int i = 0; i < nUsers; i++)
            generatedUsers.add(apiRequest.openConnection(randomUserAPI, httpServletRequest, false).toString());

        return generatedUsers;
    }
}

package com.justintime.controller;

import com.justintime.model.User;
import com.justintime.repository.UserRepository;
import com.justintime.tests.RandomUsers;
import com.justintime.utils.HttpRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    private static ArrayList<Pair<String, String>> admins = new ArrayList<>();

    static {
        admins.add(Pair.of("tperkovic@unipu.hr", "123456"));
        admins.add(Pair.of("aduda@unipu.hr", "54321"));
        admins.add(Pair.of("lpican@unipu.hr", "mahaaaCI"));
    }

    @Lazy
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    private HashMap<String, User> generatedUsersStorage = new HashMap<>();
    private HashMap<String, JSONObject> logedUsers = new HashMap<>();

    private String tokenURI = "/oauth/token?grant_type=password&username=%1$s&password=%2$s";
    private String openQueueURI = "queue/openQueue/%1$s/%2$s?access_token=%3$s";

    @RequestMapping(value = "/generate/{nUsers}", method = RequestMethod.POST)
    public ResponseEntity<?> generateUsers(@PathVariable("nUsers") String nUsers, HttpServletRequest httpServletRequest) {
        generatedUsersStorage = new RandomUsers().generate(Integer.parseInt(nUsers), httpServletRequest);
        ArrayList<User> generatedUsers = generatedUsersStorage.values().stream().map(User::clone).collect(Collectors.toCollection(ArrayList::new));
        generatedUsers.forEach(user -> user.setPassword(passwordEncoder.encode(user.getPassword())));

        userRepository.save(generatedUsers);

        for (int i = 0; i < generatedUsers.size(); i++)
            generatedUsersStorage.get(generatedUsers.get(i).getMail()).setId(generatedUsers.get(i).getId());

        return new ResponseEntity<>(generatedUsers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deleteAll", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteGeneratedUsers() {
        userRepository.delete(generatedUsersStorage.values());

        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteAll/{domain:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAllByDomain(@PathVariable("domain") String domain) {
        List<User> userList = userRepository.findAll();
        userList.removeIf(user -> !user.getMail().contains(domain));

        userRepository.delete(userList);

        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/simulate", method = RequestMethod.POST)
    public ResponseEntity<?> simulate(HttpServletRequest httpServletRequest) {
        HttpRequest request = new HttpRequest();
        String mupId = "5829f97b21ad6507d041f480";
        String[] mupRedovi = {"5878cf2bb3646416b86ba1aa", "58853e5f1a67a300047bb85a", "5878cf40b3646416b86ba1ab"};
        ArrayList<String> responses = new ArrayList<>();
        String adminUsername;
        String adminPassword;

        for (int i = 0; i < admins.size(); i++) {
            adminUsername = admins.get(i).getFirst();
            adminPassword = admins.get(i).getSecond();

            ResponseEntity<?> adminData = request.openConnection(String.format(tokenURI, adminUsername, adminPassword), "GET", httpServletRequest, true);
            logedUsers.put(adminUsername, new JSONObject(adminData.getBody().toString()));
            responses.add(String.valueOf(request.openConnection(String.format(openQueueURI, mupId, mupRedovi[i], logedUsers.get(adminUsername).get("access_token")), "POST", httpServletRequest, true).getBody()));
        }

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

}

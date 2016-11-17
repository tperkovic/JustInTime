package com.justintime.controller;

import com.justintime.model.User;
import com.justintime.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:9000")
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/create")
    public User create(User user){
        userRepository.save(user);

        return user;
    }

    @RequestMapping("/read-all")
//    @ResponseBody
    public List<User> readAll(){
        List<User> users = userRepository.findAll();

        return users;
    }

}

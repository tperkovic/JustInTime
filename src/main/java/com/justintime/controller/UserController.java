package com.justintime.controller;

import com.justintime.model.User;
import com.justintime.repository.UserRepository;
import com.justintime.utils.CustomUser;
import com.justintime.utils.NullAwareUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping("/create")
    public ResponseEntity<User> create(User user) throws Exception {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        inMemoryUserDetailsManager.createUser(new CustomUser(user.getMail(), user.getPassword(), authorities));

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/read-all")
    public ResponseEntity<List<User>> readAll(){
        List<User> users = userRepository.findAll();
        if (users.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") String id, User userParam) throws Exception {
        User user = userRepository.findByid(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (!(userParam.getPassword() == null))
            userParam.setPassword(passwordEncoder.encode(userParam.getPassword()));

        NullAwareUtilsBean.CopyProperties(userParam, user);
        userRepository.save(user);

        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        inMemoryUserDetailsManager.updateUser(new CustomUser(user.getMail(), user.getPassword(), authorities));

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/delete/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable("id") String id){
        User user = userRepository.findByid(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        userRepository.delete(user);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/id/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id){
        User user = userRepository.findByid(id);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/{mail:.+}")
    public ResponseEntity<User> getMail(@PathVariable("mail") String mail){
        User user = userRepository.findBymail(mail);
        if (user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
package com.justintime.controller;

import com.justintime.tests.RandomUsers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value = "/generate/{nUsers}", method = RequestMethod.POST)
    public ResponseEntity<?> generateUsers(@PathVariable("nUsers") String nUsers, HttpServletRequest httpServletRequest) {
        List<String> generatedUsers = new RandomUsers().generate(Integer.parseInt(nUsers), httpServletRequest);

        return new ResponseEntity<>(generatedUsers, HttpStatus.CREATED);
    }
}

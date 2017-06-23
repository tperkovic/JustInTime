package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.HashMap;

@Data
public abstract class AbstractUser {

    @Id
    public String id;
    public HashMap<String, String > googleData = new HashMap<>();
    public String firstName;
    public String lastName;
    public String mail;
    private String password;
    public String role;
}

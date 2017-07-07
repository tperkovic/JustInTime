package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class AbstractUser {

    @Id
    public String id;
    public String googleId;
    public String firstName;
    public String lastName;
    public String mail;
    private String password;
    public String role;
}

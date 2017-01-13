package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public abstract class AbstractUser {

    @Id
    public String id;
    public String firstName;
    public String lastName;
    public String mail;
    private String password;
    public String role;
}

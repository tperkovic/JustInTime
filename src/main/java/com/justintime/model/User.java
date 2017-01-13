package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public abstract class User {

    @Id
    public String id;
    public String firstName;
    public String lastName;
    public String mail;
    private String password;
    public String role;
}

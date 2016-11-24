package com.justintime.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class User {

    @Id
    private String id;
    public String firstName;
    public String lastName;
    private String mail;
    private String password;
}

package com.justintime.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User {
    @Id @Getter @Setter
    private String id;

    @Getter @Setter
    public String firstName;

    @Getter @Setter
    public String lastName;

    @Getter @Setter
    private String mail;

    @Getter @Setter
    private String password;

}

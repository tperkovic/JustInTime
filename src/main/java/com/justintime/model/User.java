package com.justintime.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class User extends AbstractUser {

    @Override
    public User clone() {
        User clone = new User();
        clone.setId(this.getId());
        clone.setGoogleId(this.getGoogleId());
        clone.setFacebookId(this.getFacebookId());
        clone.setFirstName(this.getFirstName());
        clone.setLastName(this.getLastName());
        clone.setMail(this.getMail());
        clone.setPassword(this.getPassword());
        clone.setRole(this.getRole());

        return clone;
    }
}

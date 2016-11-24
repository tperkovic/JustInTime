package com.justintime.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Facility {

    @Id
    private String id;
    public String name;
    public String address;
    public String mail;
    public String telephone;
}

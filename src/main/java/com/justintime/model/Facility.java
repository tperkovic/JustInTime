package com.justintime.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Facility {

    @Id @Getter @Setter
    private String id;

    @Getter @Setter
    public String name;



}

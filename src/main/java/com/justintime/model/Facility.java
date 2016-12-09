package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document
@Data
public class Facility {

    @Id
    private String id;
    public String name;
    public String address;
    public String mail;
    public String telephone;
    public ArrayList<Queue> queues = new ArrayList<>();
}

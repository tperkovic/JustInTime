package com.justintime.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Document
@Data
public class Queue {
    @Id
    private String id;
    private String name;
    public LinkedHashMap<Integer, User> userList = new LinkedHashMap<>();
}

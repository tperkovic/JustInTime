package com.justintime.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Document
@Data
public abstract class Queue {
    @Id
    public String id;
    public String name;
}

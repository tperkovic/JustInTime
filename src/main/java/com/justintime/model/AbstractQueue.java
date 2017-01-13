package com.justintime.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Data
public abstract class AbstractQueue {
    @Id
    public String id;
    public String name;
}

package com.justintime.model;


import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class AbstractQueue {
    @Id
    public String id;
    public String name;
}

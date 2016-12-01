package com.justintime.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
public class Facility {

    @Id
    private String id;
    public String name;
    public String address;
    public String mail;
    public String telephone;
    public List<Queue> queues = new ArrayList<>();
}

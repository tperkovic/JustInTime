package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public abstract class iFacility {
    @Id
    protected String id;
    protected String name;
    protected String address;
    protected String mail;
    protected String telephone;
}

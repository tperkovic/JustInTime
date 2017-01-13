package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
public abstract class AbstractFacility {
    @Id
    protected String id;
    protected String name;
    protected String address;
    protected String mail;
    protected String telephone;
}

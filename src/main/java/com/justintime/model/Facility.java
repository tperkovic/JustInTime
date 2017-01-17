package com.justintime.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document
public class Facility extends AbstractFacility {
    public ArrayList<Queue> queues = new ArrayList<>();
}

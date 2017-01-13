package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document
@Data
public class Facility extends iFacility {
    public ArrayList<Queue> queues = new ArrayList<>();
}

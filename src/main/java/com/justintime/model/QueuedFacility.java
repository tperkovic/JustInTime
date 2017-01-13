package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;

@Data
@Document
public class QueuedFacility extends AbstractFacility {
    public LinkedHashMap <String, QueuePriority> queues = new LinkedHashMap<>();
}

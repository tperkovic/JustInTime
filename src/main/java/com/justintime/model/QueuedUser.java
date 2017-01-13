package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class QueuedUser {
    private User user;
    private Facility facility;
    private Queue queue;
    private int priority = 0;
}

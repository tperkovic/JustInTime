package com.justintime.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Data
public class TimeHistory {
    @Id
    private String id;

    @DBRef
    private QueuePriority queuePriority;

    @DBRef
    private User user;

    @DBRef
    private User employer;

    private Instant arrivalTime;

    private Instant serviceTime;
}

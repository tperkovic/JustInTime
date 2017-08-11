package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
public class QueuePriority extends AbstractQueue {
    private Instant openingTime;

    private Instant closingTime;

    public int priority = 0;
}

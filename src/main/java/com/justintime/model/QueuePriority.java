package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class QueuePriority extends Queue{
    public int priority = 0;
}

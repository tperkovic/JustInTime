package com.justintime.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class UserInQueue {
    @DBRef
    private QueuePriority queuePriority;

    @DBRef
    private User user;
}

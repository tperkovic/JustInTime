package com.justintime.repository;

import com.justintime.model.QueuePriority;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuePriorityRepository extends MongoRepository <QueuePriority, String> {
    QueuePriority findById(String id);
}

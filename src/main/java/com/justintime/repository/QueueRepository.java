package com.justintime.repository;

import com.justintime.model.Queue;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueueRepository extends MongoRepository<Queue, String > {

    Queue findByid(String id);
}

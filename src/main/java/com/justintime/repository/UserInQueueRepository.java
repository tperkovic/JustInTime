package com.justintime.repository;

import com.justintime.model.UserInQueue;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserInQueueRepository extends MongoRepository<UserInQueue, String > {
    List<UserInQueue> findByQueuePriorityId(ObjectId id);

    @Query("{ 'user.id' : ?0 }")
    UserInQueue findByUserId(String id);
}

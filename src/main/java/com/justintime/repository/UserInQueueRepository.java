package com.justintime.repository;

import com.justintime.model.UserInQueue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserInQueueRepository extends MongoRepository<UserInQueue, String > {
    @Query("{ 'queuePriority.id' : ?0 }")
    List<UserInQueue> findByQueuePriorityId(String id);

    @Query("{ 'user.id' : ?0 }")
    UserInQueue findByUserId(String mail);
}

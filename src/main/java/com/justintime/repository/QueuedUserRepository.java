package com.justintime.repository;


import com.justintime.model.Queue;
import com.justintime.model.QueuedUser;
import com.justintime.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QueuedUserRepository extends MongoRepository<QueuedUser, String> {
    QueuedUser findByUserAndQueue(User user, Queue queue);
    List<QueuedUser> findByUser(User user);
}

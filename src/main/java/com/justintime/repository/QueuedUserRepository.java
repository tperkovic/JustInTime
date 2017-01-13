package com.justintime.repository;


import com.justintime.model.QueuedUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QueuedUserRepository extends MongoRepository<QueuedUser, String> {
    QueuedUser findByMail(String mail);
}

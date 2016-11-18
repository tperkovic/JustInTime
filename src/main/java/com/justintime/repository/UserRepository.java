package com.justintime.repository;


import com.justintime.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>{

    User findBymail(String mail);
    User findByid(String id);
}

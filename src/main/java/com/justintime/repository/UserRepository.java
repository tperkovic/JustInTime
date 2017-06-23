package com.justintime.repository;


import com.justintime.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String>{

    User findBymail(String mail);
    User findByid(String id);

    @Query("{ 'googleData.id' : ?0 }")
    User findByGoogleDataId(String id);
}

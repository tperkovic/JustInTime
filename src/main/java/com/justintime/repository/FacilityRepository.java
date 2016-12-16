package com.justintime.repository;


import com.justintime.model.Facility;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FacilityRepository extends MongoRepository<Facility, String> {

    Facility findByid(String id);

//    @Query(value = "queues.userList : ?0")
//    List<Facility> findFacilitiesByMail(String mail);
}

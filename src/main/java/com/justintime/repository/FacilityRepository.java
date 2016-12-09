package com.justintime.repository;


import com.justintime.model.Facility;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FacilityRepository extends MongoRepository<Facility, String> {

    Facility findByid(String id);
}

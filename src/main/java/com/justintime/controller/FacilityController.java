package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/facility")
public class FacilityController {

    @Autowired
    FacilityRepository facilityRepository;

    @RequestMapping("/create")
    public ResponseEntity<Facility> create(Facility facility){
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.CREATED);
    }

    @RequestMapping("/read-all")
    public ResponseEntity<List<Facility>> readAll(){
        List<Facility> facilities = facilityRepository.findAll();
        if (facilities.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(facilities, HttpStatus.OK);
    }
}

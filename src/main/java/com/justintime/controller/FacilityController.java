package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Facility create(Facility facility){
        facilityRepository.save(facility);

        return facility;
    }

    @RequestMapping("/read-all")
    public List<Facility> readAll(){
        return facilityRepository.findAll();

    }
}

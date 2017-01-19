package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.repository.FacilityRepository;
import com.justintime.utils.NullAwareUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/facility")
public class FacilityController {

    @Autowired
    FacilityRepository facilityRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<Facility> create(Facility facility){
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/read-all", method = RequestMethod.GET)
    public ResponseEntity<List<Facility>> readAll(){
        List<Facility> facilities = facilityRepository.findAll();
        if (facilities.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(facilities, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Facility> updateFacility(@PathVariable("id") String id, Facility facilityParam){
        Facility facility = facilityRepository.findByid(id);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        NullAwareUtilsBean.CopyProperties(facilityParam, facility);
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Facility> deleteFacility(@PathVariable("id") String id){
        Facility facility= facilityRepository.findByid(id);
        if (facility== null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        facilityRepository.delete(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Facility> getFacility(@PathVariable("id") String id){
        Facility facility = facilityRepository.findByid(id);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }
}

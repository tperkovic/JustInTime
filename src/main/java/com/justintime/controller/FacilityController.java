package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.model.Queue;
import com.justintime.repository.FacilityRepository;
import com.justintime.repository.QueueRepository;
import com.justintime.utils.NullAwareUtilsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/facility")
public class FacilityController {

    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    QueueRepository queueRepository;

    @RequestMapping("/create")
    public ResponseEntity<Facility> create(Facility facility){
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.CREATED);
    }

    @RequestMapping("{id}/create/queue")
    public ResponseEntity<Facility> createQueue(@PathVariable("id") String id, Queue queue){
        Facility facility = facilityRepository.findByid(id);
        queue.setId(null);
        queueRepository.save(queue);
        facility.queues.add(queue);

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

    @RequestMapping("/update/{id}")
    public ResponseEntity<Facility> updateFacility(@PathVariable("id") String id, Facility facilityParam){
        Facility facility = facilityRepository.findByid(id);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        NullAwareUtilsBean.CopyProperties(facilityParam, facility);
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @RequestMapping("/{idFacility}/update/queue/{id}")
    public ResponseEntity<Facility> updateFacilityQueue(@PathVariable("idFacility") String idFacility, @PathVariable("id") String id, Queue queueParam){
        Facility facility = facilityRepository.findByid(idFacility);
        Queue queue = queueRepository.findByid(id);
        if (facility == null || queue == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        int indexOfQueue = facility.queues.indexOf(queue);
        NullAwareUtilsBean.CopyProperties(queueParam,facility.queues.get(indexOfQueue));
        NullAwareUtilsBean.CopyProperties(queueParam, queue);

        queueRepository.save(queue);
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @RequestMapping("/delete/{id}")
    public ResponseEntity<Facility> deleteFacility(@PathVariable("id") String id){
        Facility facility= facilityRepository.findByid(id);
        if (facility== null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        facilityRepository.delete(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @RequestMapping("/{idFacility}/delete/queue/{id}")
    public ResponseEntity<Facility> deleteFacilityQueue(@PathVariable("idFacility") String idFacility, @PathVariable("id") String id, Queue queueParam){
        Facility facility= facilityRepository.findByid(idFacility);
        Queue queue = queueRepository.findByid(id);
        if (facility == null || queue == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        facility.queues.remove(queue);
        queueRepository.delete(queue);
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @RequestMapping("/{id}")
    public ResponseEntity<Facility> getFacility(@PathVariable("id") String id){
        Facility facility = facilityRepository.findByid(id);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }
}

package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.model.Queue;
import com.justintime.model.User;
import com.justintime.repository.FacilityRepository;
import com.justintime.repository.UserRepository;
import com.justintime.utils.NullAwareUtilsBean;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/queue")
public class QueueController {

    private int priority = 1;
    private LinkedHashMap<String, Integer> keyMap = new LinkedHashMap<>();

    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/create/{idFacility}", method = RequestMethod.POST)
    public ResponseEntity<Facility> createQueue(@PathVariable("idFacility") String idFacility, Queue queue){
        Facility facility = facilityRepository.findByid(idFacility);
        ObjectId oid = new ObjectId();
        queue.setId(oid.toString());
        facility.queues.put(queue.getId(), queue);

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/update/{idFacility}/{idQueue}", method = RequestMethod.PUT)
    public ResponseEntity<Facility> updateQueue(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, Queue queueParam){
        Facility facility = facilityRepository.findByid(idFacility);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Queue queue = facility.queues.get(idQueue);
        if (queue == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        NullAwareUtilsBean.CopyProperties(queueParam,queue);
        facility.queues.put(queue.getId(), queue);
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/delete/{idFacility}/{idQueue}", method = RequestMethod.DELETE)
    public ResponseEntity<Facility> deleteFacilityQueue(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue){
        Facility facility = facilityRepository.findByid(idFacility);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (facility.queues.get(idQueue) == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        facility.queues.remove(idQueue);
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/addUser/{idFacility}/{idQueue}/{mail:.+}")
    public ResponseEntity<Facility> addUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        Facility facility = facilityRepository.findByid(idFacility);
        User user = userRepository.findBymail(mail);
        if (facility == null || user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Queue queue = facility.queues.get(idQueue);
        if (queue == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        queue.userList.put(priority, user);
        keyMap.put(user.getMail(), priority);
        priority++;

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/removeUser/{idFacility}/{idQueue}/{mail:.+}")
    public ResponseEntity<Facility> removeUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        Facility facility = facilityRepository.findByid(idFacility);
        User user = userRepository.findBymail(mail);
        if (facility == null || user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Queue queue = facility.queues.get(idQueue);
        if (queue == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        queue.userList.remove(keyMap.get(user.getMail()));
        keyMap.remove(user.getMail());
        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @RequestMapping(value = "/getUser/{mail:.+}", method = RequestMethod.GET)
//    public ResponseEntity<List<Facility>> getUser(@PathVariable("mail") String mail) {
//        List<Facility> facilities = facilityRepository.findFacilitiesByMail(mail);
//        List<Facility> queuedFacilities = new ArrayList<>();
//        User user = userRepository.findBymail(mail);
//        if (user == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//
//        facilities.forEach(facility -> facility.queues.forEach(queue -> {
//            if (queue.userList.containsValue(user))
//                queuedFacilities.add(facility);
//        }));
//
//        return new ResponseEntity<>(queuedFacilities, HttpStatus.OK);
//    }

}

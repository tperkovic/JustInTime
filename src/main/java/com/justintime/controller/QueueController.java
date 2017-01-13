package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.model.Queue;
import com.justintime.model.QueuedUser;
import com.justintime.model.User;
import com.justintime.repository.FacilityRepository;
import com.justintime.repository.QueuedUserRepository;
import com.justintime.repository.UserRepository;
import com.justintime.utils.NullAwareUtilsBean;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/queue")
public class QueueController {

    private static int priority = 1;

    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QueuedUserRepository queuedUserRepository;

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
    @RequestMapping(value = "/addUser/{idFacility}/{idQueue}/{mail:.+}", method = RequestMethod.POST)
    public ResponseEntity<QueuedUser> addUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        User user = userRepository.findBymail(mail);
        Facility facility = facilityRepository.findByid(idFacility);

        if (user == null || facility == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Queue queue = facility.queues.get(idQueue);

        QueuedUser queuedUser = new QueuedUser();
        queuedUser.setUser(user);
        queuedUser.setFacility(facility);
        queuedUser.setQueue(queue);

        queuedUser.setPriority(priority++);

        queuedUserRepository.save(queuedUser);
        facilityRepository.save(facility);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/removeUser/{idFacility}/{idQueue}/{mail:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<QueuedUser> removeUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        User user = userRepository.findBymail(mail);
        Facility facility = facilityRepository.findByid(idFacility);
        QueuedUser queuedUser = queuedUserRepository.findByUserAndQueue(user, facility.queues.get(idQueue));

        if (user == null || facility == null || queuedUser == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        queuedUserRepository.delete(queuedUser);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/getUser/{mail:.+}", method = RequestMethod.GET)
    public ResponseEntity<List<QueuedUser>> getUser(@PathVariable("mail") String mail) {
        User user = userRepository.findBymail(mail);
        List<QueuedUser> queuedUsers = queuedUserRepository.findByUser(user);

        return new ResponseEntity<>(queuedUsers, HttpStatus.OK);
    }

}

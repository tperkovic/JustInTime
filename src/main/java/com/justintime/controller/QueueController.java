package com.justintime.controller;

import com.justintime.model.*;
import com.justintime.model.Queue;
import com.justintime.repository.FacilityRepository;
import com.justintime.repository.QueuePriorityRepository;
import com.justintime.repository.QueuedUserRepository;
import com.justintime.repository.UserRepository;
import com.justintime.utils.NullAwareUtilsBean;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/queue")
public class QueueController {

    @Autowired
    FacilityRepository facilityRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QueuedUserRepository queuedUserRepository;

    @Autowired
    QueuePriorityRepository queuePriorityRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/create/{idFacility}", method = RequestMethod.POST)
    public ResponseEntity<Facility> createQueue(@PathVariable("idFacility") String idFacility, Queue queue){
        Facility facility = facilityRepository.findByid(idFacility);
        ObjectId oid = new ObjectId();
        queue.setId(oid.toString());
        facility.queues.add(queue);

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/update/{idFacility}/{idQueue}", method = RequestMethod.PUT)
    public ResponseEntity<Facility> updateQueue(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, Queue queueParam){
        Facility facility = facilityRepository.findByid(idFacility);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        for (Queue q : facility.queues)
            if (q.getId().equals(idQueue)) {
                NullAwareUtilsBean.CopyProperties(queueParam,q);
                facility.queues.add(q);
            }
            else return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/delete/{idFacility}/{idQueue}", method = RequestMethod.DELETE)
    public ResponseEntity<Facility> deleteFacilityQueue(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue){
        Facility facility = facilityRepository.findByid(idFacility);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Iterator<Queue> iterator = facility.queues.iterator();
        while (iterator.hasNext()) {
            Queue queue = iterator.next();
            if (queue.getId().equals(idQueue))
                iterator.remove();
            else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/addUser/{idFacility}/{idQueue}/{mail:.+}", method = RequestMethod.POST)
    public ResponseEntity<QueuedUser> addUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        User user = userRepository.findBymail(mail);
        Facility facility = facilityRepository.findByid(idFacility);
        QueuedUser queuedUser = queuedUserRepository.findByMail(mail);
        QueuePriority queuePriority = queuePriorityRepository.findById(idQueue);

        if (user == null || facility == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (queuedUser == null) {
            queuedUser = new QueuedUser();
            NullAwareUtilsBean.CopyProperties(user, queuedUser);
        }

        if (queuePriority == null) {
            queuePriority = new QueuePriority();
            for (Queue q : facility.queues)
                if (q.getId().equals(idQueue)) {
                    NullAwareUtilsBean.CopyProperties(q, queuePriority);
                }
        }

        queuePriority.priority++;
        facility.queues.clear();

        QueuedFacility queuedFacility = new QueuedFacility();
        NullAwareUtilsBean.CopyProperties(facility, queuedFacility);
        queuedFacility.queues.put(queuePriority.getId(), queuePriority);

        if (queuedUser.queuedFacilities.get(idFacility) == null) {
            queuedUser.queuedFacilities.put(queuedFacility.getId(), queuedFacility);
        }
        else if (queuedUser.queuedFacilities.get(idFacility).queues.get(idQueue) == null) {
            queuedUser.queuedFacilities.get(idFacility).queues.put(queuePriority.getId(), queuePriority);
        }
        else return new ResponseEntity<>(HttpStatus.CONFLICT);

        queuePriorityRepository.save(queuePriority);
        queuedUserRepository.save(queuedUser);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/removeUser/{idFacility}/{idQueue}/{mail:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<QueuedUser> removeUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        QueuedUser queuedUser = queuedUserRepository.findByMail(mail);

        if (queuedUser == null || queuedUser.queuedFacilities.get(idFacility) == null || queuedUser.queuedFacilities.get(idFacility).queues.get(idQueue) == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        queuedUser.queuedFacilities.get(idFacility).queues.remove(idQueue);

        if (queuedUser.queuedFacilities.get(idFacility).queues.isEmpty())
            queuedUser.queuedFacilities.remove(idFacility);

        queuedUserRepository.save(queuedUser);

        if (queuedUser.queuedFacilities.isEmpty())
            queuedUserRepository.delete(queuedUser);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/getUser/{mail:.+}", method = RequestMethod.GET)
    public ResponseEntity<QueuedUser> getUser(@PathVariable("mail") String mail) {
        QueuedUser queuedUsers = queuedUserRepository.findByMail(mail);

        return new ResponseEntity<>(queuedUsers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/resetQueue", method = RequestMethod.DELETE)
    public ResponseEntity<List<QueuePriority>> resetQueue() {
        List<QueuePriority> queuePriorities = queuePriorityRepository.findAll();

        queuePriorities.forEach(queuePriority -> queuePriority.setPriority(0));
        queuePriorityRepository.save(queuePriorities);

        return new ResponseEntity<>(queuePriorities, HttpStatus.OK);
    }

}

package com.justintime.controller;

import com.justintime.model.*;
import com.justintime.repository.*;
import com.justintime.utils.CheckTokenRequest;
import com.justintime.utils.NullAwareUtilsBean;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

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

    @Autowired
    UserInQueueRepository userInQueueRepository;

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

        for (Iterator<Queue> iterator = facility.queues.iterator(); iterator.hasNext();) {
            Queue queue = iterator.next();
            if (queue.getId().equals(idQueue))
                NullAwareUtilsBean.CopyProperties(queueParam, queue);
        }

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
    @RequestMapping(value = "/addUser/{idFacility}/{idQueue}", method = RequestMethod.POST)
    public ResponseEntity<QueuedUser> addUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @RequestParam("access_token") String accessToken, HttpServletRequest request) {
        String username = new CheckTokenRequest().getUsername(accessToken, request);
        User user = userRepository.findBymail(username);
        Facility facility = facilityRepository.findByid(idFacility);
        QueuedUser queuedUser = queuedUserRepository.findByMail(username);
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

        UserInQueue userInQueue = new UserInQueue();
        userInQueue.setQueuePriority(queuePriority);
        userInQueue.setUser(user);

        userInQueueRepository.save(userInQueue);
        queuePriorityRepository.save(queuePriority);
        queuedUserRepository.save(queuedUser);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/removeUser/{idFacility}/{idQueue}", method = RequestMethod.DELETE)
    public ResponseEntity<QueuedUser> removeUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @RequestParam("access_token") String accessToken, HttpServletRequest request) {
        String username = new CheckTokenRequest().getUsername(accessToken, request);
        QueuedUser queuedUser = queuedUserRepository.findByMail(username);

        if (queuedUser == null || queuedUser.queuedFacilities.get(idFacility) == null || queuedUser.queuedFacilities.get(idFacility).queues.get(idQueue) == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        UserInQueue userInQueue = userInQueueRepository.findByUserId(queuedUser.getId());

        if (userInQueue != null) {
            userInQueueRepository.delete(userInQueue);
        }

        queuedUser.queuedFacilities.get(idFacility).queues.remove(idQueue);

        if (queuedUser.queuedFacilities.get(idFacility).queues.isEmpty()) {
            queuedUser.queuedFacilities.remove(idFacility);
        }

        queuedUserRepository.save(queuedUser);

        if (queuedUser.queuedFacilities.isEmpty()) {
            queuedUserRepository.delete(queuedUser);
        }

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/getQueuedUser", method = RequestMethod.GET)
    public ResponseEntity<QueuedUser> getQueuedUser(@RequestParam("access_token") String accessToken, HttpServletRequest request) {
        String username = new CheckTokenRequest().getUsername(accessToken, request);
        QueuedUser queuedUser = queuedUserRepository.findByMail(username);
        if (queuedUser == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @RequestMapping(value = "/{queueId}", method = RequestMethod.GET)
    public ResponseEntity<QueuePriority> getPriorityQueue(@PathVariable("queueId") String queueId) {
        QueuePriority queuePriority = queuePriorityRepository.findById(queueId);
        if (queuePriority == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(queuePriority, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/getUser/{mail:.+}", method = RequestMethod.GET)
    public ResponseEntity<QueuedUser> getUser(@PathVariable("mail") String mail) {
        QueuedUser queuedUser = queuedUserRepository.findByMail(mail);
        if (queuedUser == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/getAllUsers/{queueId}", method = RequestMethod.GET)
    public ResponseEntity<List<UserInQueue>> getAllQueuedUsers(@PathVariable("queueId") String queueId) {
        List<UserInQueue> userInQueues = userInQueueRepository.findByQueuePriorityId(queueId);
        if (userInQueues.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(userInQueues, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/resetQueue/{queueId}", method = RequestMethod.DELETE)
    public ResponseEntity<QueuePriority> resetQueue(@PathVariable("queueId") String queueId) {
        QueuePriority queuePriority = queuePriorityRepository.findById(queueId);

        queuePriority.setPriority(0);
        queuePriorityRepository.save(queuePriority);

        return new ResponseEntity<>(queuePriority, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/resetQueues", method = RequestMethod.DELETE)
    public ResponseEntity<List<QueuePriority>> resetQueues() {
        List<QueuePriority> queuePriorities = queuePriorityRepository.findAll();

        queuePriorities.forEach(queuePriority -> queuePriority.setPriority(0));
        queuePriorityRepository.save(queuePriorities);

        return new ResponseEntity<>(queuePriorities, HttpStatus.OK);
    }

}

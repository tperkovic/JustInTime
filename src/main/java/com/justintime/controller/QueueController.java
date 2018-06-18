package com.justintime.controller;

import com.justintime.model.*;
import com.justintime.repository.*;
import com.justintime.utils.NullAwareUtilsBean;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

    @Autowired
    TimeHistoryRepository timeHistoryRepository;

    private LinkedHashMap<String, UserInQueue> currentUser = new LinkedHashMap<>();
    
    private boolean removeUserFromRepository(QueuedUser queuedUser, UserInQueue userInQueue, String idFacility, String idQueue) {
        if (queuedUser == null || queuedUser.queuedFacilities.get(idFacility) == null || queuedUser.queuedFacilities.get(idFacility).queues.get(idQueue) == null)
            return false;

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
        
        return true;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/create/{idFacility}", method = RequestMethod.POST)
    public ResponseEntity<Facility> createFacilityQueue(@PathVariable("idFacility") String idFacility, Queue queue){
        Facility facility = facilityRepository.findByid(idFacility);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        ObjectId oid = new ObjectId();
        queue.setId(oid.toString());
        facility.queues.add(queue);

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/update/{idFacility}/{idQueue}", method = RequestMethod.PUT)
    public ResponseEntity<Facility> updateFacilityQueue(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, Queue queueParam){
        Facility facility = facilityRepository.findByid(idFacility);
        if (facility == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        for (Queue queue : facility.queues) {
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
    public ResponseEntity<QueuedUser> addUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, Principal principal) {
        String username = principal.getName();

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

        TimeHistory timeHistory = new TimeHistory();
        timeHistory.setQueuePriority(queuePriority);
        timeHistory.setUser(user);
        timeHistory.setArrivalTime(Instant.now());

        userInQueueRepository.save(userInQueue);
        queuePriorityRepository.save(queuePriority);
        queuedUserRepository.save(queuedUser);
        timeHistoryRepository.save(timeHistory);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/removeUser/{idFacility}/{idQueue}", method = RequestMethod.DELETE)
    public ResponseEntity<QueuedUser> removeUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, Principal principal) {
        String username = principal.getName();
        QueuedUser queuedUser = queuedUserRepository.findByMail(username);

        if (queuedUser == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        UserInQueue userInQueue = userInQueueRepository.findByUserId(queuedUser.getId());

        if (!removeUserFromRepository(queuedUser, userInQueue, idFacility, idQueue))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        TimeHistory timeHistory = timeHistoryRepository.findByQueuePriorityAndUser(userInQueue.getQueuePriority(), userInQueue.getUser());
        if (timeHistory != null)
            timeHistoryRepository.delete(timeHistory);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @RequestMapping(value = "/getQueuedUser", method = RequestMethod.GET)
    public ResponseEntity<QueuedUser> getQueuedUser(Principal principal) {
        String username = principal.getName();
        QueuedUser queuedUser = queuedUserRepository.findByMail(username);
        if (queuedUser == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @RequestMapping(value = "/{idQueue}", method = RequestMethod.GET)
    public ResponseEntity<String> getNumberOfQueuedUsers(@PathVariable("idQueue") String idQueue) {
        List<UserInQueue> userInQueues = userInQueueRepository.findByQueuePriorityId(new ObjectId(idQueue));
        if (userInQueues == null || userInQueues.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        String numberOfUsers = String.format("{\"number\":%d}", userInQueues.size());

        return new ResponseEntity<>(numberOfUsers, HttpStatus.OK);
    }

    @RequestMapping(value = "/getCurrentUserNumber/{idQueue}", method = RequestMethod.GET)
    public ResponseEntity<String> getCurrentUserNumber(@PathVariable("idQueue") String idQueue) {
        if (currentUser == null || currentUser.isEmpty() || !currentUser.containsKey(idQueue))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        int currentNumber = currentUser.get(idQueue).getQueuePriority().getPriority();

        String currentUserNumber = String.format("{\"currentNumber\":%d}", currentNumber);

        return new ResponseEntity<>(currentUserNumber, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/nextUser/{idFacility}/{idQueue}", method = RequestMethod.POST)
    public ResponseEntity<Void> nextUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, Principal principal) {
        List<UserInQueue> userInQueues = userInQueueRepository.findByQueuePriorityId(new ObjectId(idQueue));
        if (userInQueues == null ||userInQueues.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        String employerUsername = principal.getName();
        User employer = userRepository.findBymail(employerUsername);
        if (employer == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        UserInQueue userInQueue = new UserInQueue();
        NullAwareUtilsBean.CopyProperties(userInQueues.get(0), userInQueue);
        QueuedUser queuedUser = queuedUserRepository.findByMail(userInQueue.getUser().getMail());

        currentUser.put(idQueue, userInQueue);

        if (!removeUserFromRepository(queuedUser, userInQueue, idFacility, idQueue))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        TimeHistory timeHistory = timeHistoryRepository.findByQueuePriorityAndUser(userInQueue.getQueuePriority(), userInQueue.getUser());
        if (timeHistory != null) {
            timeHistory.setEmployer(employer);
            timeHistory.setServiceTime(Instant.now());
            timeHistoryRepository.save(timeHistory);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/getQueuedUser/{mail:.+}", method = RequestMethod.GET)
    public ResponseEntity<QueuedUser> getQueuedUserByMail(@PathVariable("mail") String mail) {
        QueuedUser queuedUser = queuedUserRepository.findByMail(mail);
        if (queuedUser == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(queuedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/getAllQueuedUsers/{idQueue}", method = RequestMethod.GET)
    public ResponseEntity<List<UserInQueue>> getAllQueuedUsers(@PathVariable("idQueue") String idQueue) {
        List<UserInQueue> userInQueues = userInQueueRepository.findByQueuePriorityId(new ObjectId(idQueue));
        if (userInQueues.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(userInQueues, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/resetQueue/{idQueue}", method = RequestMethod.DELETE)
    public ResponseEntity<QueuePriority> resetQueue(@PathVariable("idQueue") String idQueue) {
        QueuePriority queuePriority = queuePriorityRepository.findById(idQueue);

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

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/openQueue/{idFacility}/{idQueue}", method = RequestMethod.POST)
    public ResponseEntity<?> openQueue(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue,
                                       @RequestParam(defaultValue = "8", required = false) Integer hours, Principal principal) {

        QueuePriority queuePriority = new QueuePriority();
        String username = principal.getName();

        Facility facility = facilityRepository.findByid(idFacility);
        facility.queues.forEach(queue -> {
            if (queue.getId().equals(idQueue)) {
                NullAwareUtilsBean.CopyProperties(queue, queuePriority);
            }
        });

        long seconds = hours.longValue() * 3600;

        queuePriority.setOpeningTime(Instant.now());
        queuePriority.setClosingTime(Instant.now().plusSeconds(seconds));
        queuePriorityRepository.save(queuePriority);

        return new ResponseEntity<>(String.format("Welcome %1$s, Queue '%2$s' is now opened!", username, queuePriority.getName()), HttpStatus.OK);
    }

}

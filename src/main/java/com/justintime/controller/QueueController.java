package com.justintime.controller;

import com.justintime.model.Facility;
import com.justintime.model.Queue;
import com.justintime.model.User;
import com.justintime.repository.FacilityRepository;
import com.justintime.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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

    @RequestMapping("/addUser/{idFacility}/{idQueue}/{mail:.+}")
    public ResponseEntity<Facility> addUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        Facility facility = facilityRepository.findByid(idFacility);
        User user = userRepository.findBymail(mail);
        if (facility == null || user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        ArrayList<Queue> queues = facility.getQueues();
        queues.forEach(queue -> {
            if (queue.getId().equals(idQueue)) {
                queue.userList.put(priority, user);
                keyMap.put(user.getMail(), priority);
                priority++;
            }
        });

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @RequestMapping("/removeUser/{idFacility}/{idQueue}/{mail:.+}")
    public ResponseEntity<Facility> removeUser(@PathVariable("idFacility") String idFacility, @PathVariable("idQueue") String idQueue, @PathVariable("mail") String mail) {
        Facility facility = facilityRepository.findByid(idFacility);
        User user = userRepository.findBymail(mail);
        if (facility == null || user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        ArrayList<Queue> queues = facility.getQueues();
        queues.forEach(queue -> {
            if (queue.getId().equals(idQueue)) {
                queue.userList.remove(keyMap.get(user.getMail()));
            }
        });

        facilityRepository.save(facility);

        return new ResponseEntity<>(facility, HttpStatus.OK);
    }

    @RequestMapping("/getUser/{mail:.+}")
    public ResponseEntity<List<Facility>> getUser(@PathVariable("mail") String mail) {
        List<Facility> facilities = facilityRepository.findAll();
        List<Facility> queuedFacilities = new ArrayList<>();
        User user = userRepository.findBymail(mail);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        facilities.forEach(facility -> facility.queues.forEach(queue -> {
            if (queue.userList.containsValue(user))
                queuedFacilities.add(facility);
        }));

        return new ResponseEntity<>(queuedFacilities, HttpStatus.OK);
    }

}

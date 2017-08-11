package com.justintime.repository;

import com.justintime.model.QueuePriority;
import com.justintime.model.TimeHistory;
import com.justintime.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface TimeHistoryRepository extends MongoRepository<TimeHistory, String> {

    TimeHistory findByQueuePriorityAndUser(QueuePriority queuePriority, User user);
    List<TimeHistory> findByQueuePriorityAndArrivalTimeBetweenAndServiceTime(QueuePriority queuePriority, Instant startTime, Instant endTime, Instant servedTime);
    List<TimeHistory> findByQueuePriorityAndEmployerAndServiceTimeBetween(QueuePriority queuePriority, User employer, Instant startTime, Instant endTime);
}

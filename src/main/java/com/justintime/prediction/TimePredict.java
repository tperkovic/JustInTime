package com.justintime.prediction;

import com.justintime.model.QueuePriority;
import com.justintime.model.TimeHistory;
import com.justintime.model.User;
import com.justintime.repository.TimeHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TimePredict {

    @Autowired
    private TimeHistoryRepository timeHistoryRepository;

    public double calculateArrivalRateInSeconds(QueuePriority queuePriority) {
        List<TimeHistory> timeHistories = timeHistoryRepository.findByQueuePriorityAndArrivalTimeBetweenAndServiceTime(queuePriority, queuePriority.getOpeningTime(), queuePriority.getClosingTime(), null);
        if (timeHistories == null)
            return 0;

        TimeHistory lastTimeHistory = timeHistories.get(timeHistories.size() - 1);

        long timePassedInSeconds = Duration.between(queuePriority.getOpeningTime(), lastTimeHistory.getArrivalTime()).toMillis() / 1000;

        return timeHistories.size() / timePassedInSeconds;
    }

    public double calculateServiceRateInSeconds(QueuePriority queuePriority, User employer) {
        List<TimeHistory> timeHistories = timeHistoryRepository.findByQueuePriorityAndEmployerAndServiceTimeBetween(queuePriority, employer, Instant.now().minus(7, ChronoUnit.DAYS), Instant.now());
        if (timeHistories == null)
            return 12 / 3600;

        return timeHistories.size() / (40 * 3600);
    }

    public double expectedWaitTimeInQueue(double arrivalRate, double serviceRate) {
        return arrivalRate / (serviceRate * (serviceRate - arrivalRate));
    }
}

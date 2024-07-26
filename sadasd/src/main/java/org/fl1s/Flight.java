package org.fl1s;

import java.time.LocalDateTime;
import java.time.Duration;

public record Flight(
        String carrier,
        String departureCity,
        String destinationCity,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime, double price) {

    public Duration getFlightDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    @Override
    public String toString() {
        return "Flight{" +
                "carrier='" + carrier + '\'' +
                ", departureCity='" + departureCity + '\'' +
                ", destinationCity='" + destinationCity + '\'' +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", price=" + price +
                '}';
    }
}

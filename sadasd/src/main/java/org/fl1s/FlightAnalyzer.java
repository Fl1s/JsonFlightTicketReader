package org.fl1s;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FlightAnalyzer {

    @Data
    static class Ticket {
        private String origin;
        private String origin_name;
        private String destination;
        private String destination_name;
        private String departure_date;
        private String departure_time;
        private String arrival_date;
        private String arrival_time;
        private String carrier;
        private int stops;
        private int price;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java FlightAnalyzer <path_to_tickets.json>");
            return;
        }

        String filePath = args[0];
        ObjectMapper objectMapper = new ObjectMapper();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        try {
            Map<String, List<Ticket>> data = objectMapper.readValue(new File(filePath), new TypeReference<>() {});
            List<Ticket> tickets = data.get("tickets");

            if (tickets == null) {
                System.out.println("No tickets found in the JSON file.");
                return;
            }

            List<Ticket> vlvToTelAvivTickets = tickets.stream()
                    .filter(t -> "VVO".equals(t.getOrigin()) && "TLV".equals(t.getDestination()))
                    .collect(Collectors.toList());

            if (vlvToTelAvivTickets.isEmpty()) {
                System.out.println("No tickets found between Владивосток and Тель-Авив.");
                return;
            }

            // Расчет минимального времени полета для каждого авиаперевозчика
            Map<String, Duration> minFlightTimes = new HashMap<>();
            for (Ticket ticket : vlvToTelAvivTickets) {
                LocalTime departureTime = LocalTime.parse(ticket.getDeparture_time(), timeFormatter);
                LocalTime arrivalTime = LocalTime.parse(ticket.getArrival_time(), timeFormatter);
                Duration flightDuration = Duration.between(departureTime, arrivalTime);

                minFlightTimes.compute(ticket.getCarrier(), (carrier, existingDuration) ->
                        (existingDuration == null || flightDuration.compareTo(existingDuration) < 0) ? flightDuration : existingDuration);
            }

            System.out.println("Минимальное время полета между Владивостоком и Тель-Авивом для каждого авиаперевозчика:");
            minFlightTimes.forEach((carrier, duration) -> {
                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                System.out.printf("%s: %d ч %d мин%n", carrier, hours, minutes);
            });

            // Расчет разницы между средней ценой и медианой
            List<Integer> prices = vlvToTelAvivTickets.stream()
                    .map(Ticket::getPrice)
                    .sorted()
                    .collect(Collectors.toList());

            double median;
            int size = prices.size();
            if (size % 2 == 0) {
                median = (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
            } else {
                median = prices.get(size / 2);
            }

            double average = prices.stream().mapToDouble(Integer::doubleValue).average().orElse(0);
            double difference = average - median;

            System.out.printf("Разница между средней ценой и медианой: %.2f%n", difference);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

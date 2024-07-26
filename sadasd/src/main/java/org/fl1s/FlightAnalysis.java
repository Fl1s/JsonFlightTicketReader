package org.fl1s;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class FlightAnalysis {

    public static void main(@NotNull String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java FlightAnalysis <path-to-json-file>");
            return;
        }

        String filePath = args[0];
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, Flight.class);
        List<Flight> flights = mapper.readValue(new File(filePath), listType);

        Map<String, List<Flight>> flightsByCarrier = flights.stream()
                .filter(flight -> "Владивосток".equals(flight.departureCity()) && "Тель-Авив".equals(flight.destinationCity()))
                .collect(Collectors.groupingBy(Flight::carrier));

        System.out.println("Минимальное время полета между Владивостоком и Тель-Авивом для каждого авиаперевозчика:");
        flightsByCarrier.forEach((carrier, carrierFlights) -> {
            Duration minDuration = carrierFlights.stream()
                    .map(Flight::getFlightDuration)
                    .min(Duration::compareTo)
                    .orElse(Duration.ZERO);
            System.out.printf("%s: %s\n", carrier, minDuration);
        });

        List<Double> prices = flights.stream()
                .filter(flight -> "Владивосток".equals(flight.departureCity()) && "Тель-Авив".equals(flight.destinationCity()))
                .map(Flight::price)
                .collect(Collectors.toList());

        if (prices.isEmpty()) {
            System.out.println("Нет данных для анализа цен.");
            return;
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        prices.forEach(stats::addValue);

        double mean = stats.getMean();
        double median = calculateMedian(prices);

        System.out.printf("Разница между средней ценой и медианой: %.2f\n", mean - median);
    }

    private static double calculateMedian(List<Double> prices) {
        List<Double> sortedPrices = new ArrayList<>(prices);
        Collections.sort(sortedPrices);

        int size = sortedPrices.size();
        if (size % 2 == 0) {
            return (sortedPrices.get(size / 2 - 1) + sortedPrices.get(size / 2)) / 2.0;
        } else {
            return sortedPrices.get(size / 2);
        }
    }
}

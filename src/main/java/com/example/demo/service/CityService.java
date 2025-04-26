package com.example.demo.service;

import com.example.demo.model.CityInfo;
import com.example.demo.model.TimeInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CityService {

    private final List<CityInfo> cities = new ArrayList<>();

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cities.csv")), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine(); // пропустить заголовок
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    cities.add(new CityInfo(
                            parts[0],
                            parts[1],
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]),
                            parts[4],
                            new TimeInfo()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CityInfo> getAllCities() {
        return enrichCitiesWithTime(cities);
    }

    public CityInfo getCityByName(String name) {
        return cities.stream()
                .filter(c -> c.getCity().equalsIgnoreCase(name))
                .map(this::enrichCityWithTime)
                .findFirst()
                .orElse(null);
    }

    public List<CityInfo> getCityByCountry(String country) {
        List<CityInfo> filteredCountry = cities.stream()
                .filter(c -> c.getCountry().equalsIgnoreCase(country))
                .toList();
        return enrichCitiesWithTime(filteredCountry);
    }

    public List<CityInfo> getCityByTimeZone(String timeZone) {
        List<CityInfo> filteredCountry = cities.stream()
                .filter(c -> c.getTimezone().equalsIgnoreCase(timeZone))
                .toList();
        return enrichCitiesWithTime(filteredCountry);
    }

    public TimeInfo getTime(String cityName){
        CityInfo city = cities.stream()
                .filter(c -> c.getCity().equalsIgnoreCase(cityName))
                .map(this::enrichCityWithTime)
                .findFirst()
                .orElse(null);
        return enrichCityWithTime(city).getTimeInfo();
    }

    private List<CityInfo> enrichCitiesWithTime(List<CityInfo> cityList) {
        List<CityInfo> updated = new ArrayList<>();
        for (CityInfo city : cityList) {
            updated.add(enrichCityWithTime(city));
        }
        return updated;
    }

    private CityInfo enrichCityWithTime(CityInfo city) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(city.getTimezone()));
            city.getTimeInfo().setLocalTime(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            city.getTimeInfo().setUtcTime(Instant.now().toString()); // UFC-формат (ISO 8601, UTC)
            city.getTimeInfo().setTimeDescription(city.getCity() + " " + now.format(DateTimeFormatter.ofPattern("HH:mm '('X' UTC)'")));

        } catch (Exception e) {
            city.getTimeInfo().setLocalTime("Unknown");
            city.getTimeInfo().setUtcTime("Unknown");
            city.getTimeInfo().setTimeDescription("Unknown");
        }
        return city;
    }
}
package com.example.demo.service;

import com.example.demo.model.CityInfo;
import com.example.demo.model.TimeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.*;

@Service
public class CityServiceImpl implements CityService {

    private final List<CityInfo> cities = new ArrayList<>();
    @Value("${unsplash.access.key}")
    private String accessKey;
    private RestTemplate restTemplate = new  RestTemplate();
    private static final List<String> FALLBACK_IMAGES = List.of(
            "https://images.unsplash.com/photo-1464983953574-0892a716854b",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
            "https://images.unsplash.com/photo-1502602898657-3e91760cbb34"
    );

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cities.csv")), StandardCharsets.UTF_8))) {

            String line;
            reader.readLine(); // пропустить заголовок
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String name = parts[0].trim();
                    String country = parts[1].trim();
                    double latitude = Double.parseDouble(parts[2]);
                    double longitude = Double.parseDouble(parts[3]);
                    String timezone = parts[4].trim();
                    long population = Long.parseLong(parts[5].trim());
                    String imageUrl = getImageUrlFromPexels(name);

                    cities.add(new CityInfo(
                            name,
                            country,
                            latitude,
                            longitude,
                            timezone,
                            new TimeInfo(),
                            population,
                            imageUrl
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Value("${pexels.api.key}")
    private String pexelsApiKey;

    @Cacheable("pexelsImages")
    public String getImageUrlFromPexels(String cityName) {
        try {
            String url = "https://api.pexels.com/v1/search?query=" + URLEncoder.encode(cityName, StandardCharsets.UTF_8)
                    + "&per_page=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pexelsApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode results = root.get("results");
                if (results != null && results.size() > 0) {
                    return results.get(0).get("urls").get("regular").asText();
                }
            }
        } catch (Exception e) {
            System.out.println("Pexels error (" + cityName + "): " + e.getMessage());
        }
        return getFallbackImage(cityName);
    }


    @Cacheable("unsplashImages")
    public String getImageUrl(String cityName) {
        if (accessKey == null || accessKey.isBlank()) {
            return getFallbackImage(cityName);
        }

        try {
            String url = "https://api.unsplash.com/search/photos?query=" + cityName +
                    "&client_id=" + accessKey +
                    "&orientation=landscape&per_page=1";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode results = root.get("results");
                if (results != null && results.size() > 0) {
                    return results.get(0).get("urls").get("regular").asText();
                }
            }
        } catch (Exception e) {
            System.out.println("Unsplash error (" + cityName + "): " + e.getMessage());
        }

        return getFallbackImage(cityName);
    }
    private String getFallbackImage(String cityName) {
        int idx = Math.abs(cityName.hashCode()) % FALLBACK_IMAGES.size();
        return FALLBACK_IMAGES.get(idx);
    }

    @Cacheable("cities")
    public List<CityInfo> getAllCities() {
        return enrichCitiesWithTime(cities);
    }

    @Cacheable(value = "city", key = "#name")
    public CityInfo getCityByName(String name) {
        return cities.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .map(this::enrichCityWithTime)
                .map(city -> {
                    city.setImageUrl(getImageUrl(city.getName()));
                    return city;
                })
                .findFirst()
                .orElse(null);
    }

    @Cacheable(value = "citiesByCountry", key = "#country")
    public List<CityInfo> getCityByCountry(String country) {
        List<CityInfo> filteredCountry = cities.stream()
                .filter(c -> c.getCountry().equalsIgnoreCase(country))
                .toList();
        return enrichCitiesWithTime(filteredCountry);
    }
    @Cacheable(value = "citiesByTimeZone", key = "#timeZone")
    public List<CityInfo> getCityByTimeZone(String timeZone) {
        List<CityInfo> filteredCountry = cities.stream()
                .filter(c -> c.getTimezone().equalsIgnoreCase(timeZone))
                .toList();
        return enrichCitiesWithTime(filteredCountry);
    }
    @Cacheable(value = "searchCities", key = "#query")
    public TimeInfo getTime(String cityName){
        CityInfo city = cities.stream()
                .filter(c -> c.getName().equalsIgnoreCase(cityName))
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
            city.getTimeInfo().setTimeDescription(city.getName() + " " + now.format(DateTimeFormatter.ofPattern("HH:mm '('X' UTC)'")));

        } catch (Exception e) {
            city.getTimeInfo().setLocalTime("Unknown");
            city.getTimeInfo().setUtcTime("Unknown");
            city.getTimeInfo().setTimeDescription("Unknown");
        }
        return city;
    }
    public List<CityInfo> searchCities(String query) {
        if (query == null || query.isBlank()) {
            return getAllCities();
        }

        return cities.stream()
                .filter(city ->
                        city.getName().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());
    }
}
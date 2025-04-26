package com.example.demo.controller;

import com.example.demo.model.CityInfo;
import com.example.demo.model.TimeInfo;
import com.example.demo.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public List<CityInfo> getAllCities() {
        return cityService.getAllCities();
    }

    @GetMapping("/city/{name}")
    public CityInfo getCityByName(@PathVariable String name) {
        return cityService.getCityByName(name);
    }

    @GetMapping("/country/{country}")
    public List<CityInfo> getCityByCountry(@PathVariable String country) {
        return cityService.getCityByCountry(country);
    }

    @GetMapping("/time/{timeZone}")
    public List<CityInfo> getCityByTimeZone(@PathVariable String timeZone){
        return cityService.getCityByTimeZone(timeZone);
    }

    @GetMapping("/times/{nameCity}")
    public TimeInfo getTime(@PathVariable String nameCity){
        return cityService.getTime(nameCity);
    }
}
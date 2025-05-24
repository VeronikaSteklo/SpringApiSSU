package com.example.demo.controller;

import com.example.demo.model.CityInfo;
import com.example.demo.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Controller
@RequiredArgsConstructor
public class CityViewController {

    private final CityService cityService;

    @GetMapping("/country/{country}")
    public List<CityInfo> getCityByCountry(@PathVariable String country) {
        return cityService.getCityByCountry(country);
    }

    @GetMapping("/time/{timeZone}")
    public List<CityInfo> getCityByTimeZone(@PathVariable String timeZone){
        return cityService.getCityByTimeZone(timeZone);
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("cities", cityService.getAllCities());
        return "index";
    }

    @GetMapping("/city/{name}")
    public String cityDetails(@PathVariable String name, Model model) {
        CityInfo city = cityService.getCityByName(name);
        double probabilityBobik = 0.2;
        Random random = new Random();
        if (random.nextDouble(0,1) < probabilityBobik){
            return "bobek";
        }
        if (city == null) throw new CityNotFoundException();
        model.addAttribute("city", city);
        return "city";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<CityInfo> results = cityService.searchCities(query);
        model.addAttribute("cities", results);
        return "index";
    }

    @ExceptionHandler(CityNotFoundException.class)
    public String handleError() {
        return "error";
    }
}
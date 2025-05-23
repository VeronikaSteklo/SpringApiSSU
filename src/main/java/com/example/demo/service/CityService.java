package com.example.demo.service;

import com.example.demo.model.CityInfo;
import com.example.demo.model.TimeInfo;

import java.util.List;

public interface CityService {
    CityInfo getCityByName(String name);
    List<CityInfo> getAllCities();
    List<CityInfo> getCityByCountry(String country);
    List<CityInfo> getCityByTimeZone(String name);

    List<CityInfo> searchCities(String query);
}

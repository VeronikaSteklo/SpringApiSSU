package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityInfo {
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private String timezone;
    private TimeInfo timeInfo;

}
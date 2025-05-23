package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityInfo {
    private String name;
    private String country;
    private double latitude;
    private double longitude;
    private String timezone;
    private TimeInfo timeInfo;
    private long population; // население города
    private String imageUrl; // URL изображения города
}
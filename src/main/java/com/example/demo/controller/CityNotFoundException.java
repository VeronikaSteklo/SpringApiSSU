package com.example.demo.controller;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException() {
        super("City not found");
    }
}

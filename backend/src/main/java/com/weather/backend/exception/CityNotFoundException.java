package com.weather.backend.exception;

public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException(String city) {
        super("No coordinates found for city: '" + city + "'");
    }
}

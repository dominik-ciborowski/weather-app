package com.weather.backend.model;

public record GeocodingCoordinates(
    String cityName,
    double lat,
    double lon,
    String country
) {
}

package com.weather.backend.dto.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingLocationResponse(
    String name,
    double lat,
    double lon,
    String country
) {
}

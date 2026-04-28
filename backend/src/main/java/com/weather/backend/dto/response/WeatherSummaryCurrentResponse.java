package com.weather.backend.dto.response;

public record WeatherSummaryCurrentResponse(
    double temperature,
    double feelsLike,
    int humidity,
    double windSpeed,
    int windDirection,
    double uvIndex,
    String description,
    String icon
) {
}

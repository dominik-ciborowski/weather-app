package com.weather.backend.dto.response;

import java.time.Instant;

public record HourlyForecastItemResponse(
    Instant time,
    double temperature,
    double feelsLike,
    int humidity,
    double windSpeed,
    int windDirection,
    double precipitationProbability,
    String description,
    String icon
) {
}

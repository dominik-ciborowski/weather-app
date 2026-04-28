package com.weather.backend.dto.response;

import java.time.Instant;

public record DailyForecastItemResponse(
    Instant date,
    double minTemperature,
    double maxTemperature,
    double precipitationProbability,
    String description
) {
}

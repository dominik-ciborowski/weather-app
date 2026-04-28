package com.weather.backend.dto.response;

import java.time.Instant;

public record WeatherAlertItemResponse(
    String source,
    String event,
    Instant start,
    Instant end,
    String description
) {
}

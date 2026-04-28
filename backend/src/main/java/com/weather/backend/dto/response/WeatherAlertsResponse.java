package com.weather.backend.dto.response;

import java.util.List;

public record WeatherAlertsResponse(
    String city,
    List<WeatherAlertItemResponse> alerts
) {
}

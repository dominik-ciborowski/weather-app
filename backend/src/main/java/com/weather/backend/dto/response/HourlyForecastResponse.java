package com.weather.backend.dto.response;

import java.util.List;

public record HourlyForecastResponse(
    String city,
    String units,
    List<HourlyForecastItemResponse> hours
) {
}

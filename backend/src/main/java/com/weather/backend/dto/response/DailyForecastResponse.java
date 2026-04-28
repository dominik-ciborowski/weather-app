package com.weather.backend.dto.response;

import java.util.List;

public record DailyForecastResponse(
    String city,
    String units,
    List<DailyForecastItemResponse> days
) {
}

package com.weather.backend.dto.response;

import java.util.List;

public record WeatherSummaryResponse(
    String city,
    String units,
    WeatherSummaryCurrentResponse current,
    List<DailyForecastItemResponse> dailyForecast,
    List<WeatherAlertItemResponse> alerts
) {
}

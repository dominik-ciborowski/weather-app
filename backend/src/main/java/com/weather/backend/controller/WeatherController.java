package com.weather.backend.controller;

import com.weather.backend.dto.response.CurrentWeatherResponse;
import com.weather.backend.dto.response.DailyForecastResponse;
import com.weather.backend.dto.response.HourlyForecastResponse;
import com.weather.backend.dto.response.WeatherAlertsResponse;
import com.weather.backend.dto.response.WeatherSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.weather.backend.model.Unit;
import com.weather.backend.service.WeatherService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/weather")
@Tag(name = "Weather", description = "Weather lookup, forecasts, alerts, and combined summaries")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    @Operation(summary = "Get current weather for a city")
    public CurrentWeatherResponse currentWeather(
        @RequestParam String city,
        @RequestParam(defaultValue = "metric") Unit units
    ) {

        return weatherService.getCurrentWeather(city, units);
    }

    @GetMapping("/forecast/hourly")
    @Operation(summary = "Get hourly forecast for a city")
    public HourlyForecastResponse hourlyForecast(
        @RequestParam String city,
        @RequestParam @Min(1) @Max(48) int hours,
        @RequestParam(defaultValue = "metric") Unit units
    ) {

        return weatherService.getHourlyForecast(city, hours, units);
    }

    @GetMapping("/forecast/daily")
    @Operation(summary = "Get daily forecast for a city")
    public DailyForecastResponse dailyForecast(
        @RequestParam String city,
        @RequestParam @Min(1) @Max(8) int days,
        @RequestParam(defaultValue = "metric") Unit units
    ) {

        return weatherService.getDailyForecast(city, days, units);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get active government weather alerts for a city")
    public WeatherAlertsResponse weatherAlerts(@RequestParam String city) {
        return weatherService.getWeatherAlerts(city);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get current weather, 7-day forecast, and active alerts in one response")
    public WeatherSummaryResponse weatherSummary(
        @RequestParam String city,
        @RequestParam(defaultValue = "metric") Unit units
    ) {

        return weatherService.getWeatherSummary(city, units);
    }

}

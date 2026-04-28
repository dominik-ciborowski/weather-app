package com.weather.backend.dto.openweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OneCallResponse(
    CurrentWeather current,
    List<HourlyWeather> hourly,
    List<DailyWeather> daily,
    List<WeatherAlert> alerts
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentWeather(
        double temp,
        double feels_like,
        int humidity,
        double wind_speed,
        int wind_deg,
        double uvi,
        List<WeatherDescription> weather
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HourlyWeather(
        long dt,
        double temp,
        double feels_like,
        int humidity,
        double wind_speed,
        int wind_deg,
        double pop,
        List<WeatherDescription> weather
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DailyWeather(
        long dt,
        TemperatureRange temp,
        double pop,
        List<WeatherDescription> weather
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TemperatureRange(
        double min,
        double max
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherAlert(
        String sender_name,
        String event,
        long start,
        long end,
        String description
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherDescription(
        String main,
        String description,
        String icon
    ) {
    }
}

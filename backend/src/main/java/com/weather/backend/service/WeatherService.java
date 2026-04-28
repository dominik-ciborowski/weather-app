package com.weather.backend.service;

import com.weather.backend.client.GeocodingClient;
import com.weather.backend.client.OneCallClient;
import com.weather.backend.config.CacheConfig;
import com.weather.backend.dto.response.CurrentWeatherResponse;
import com.weather.backend.dto.response.DailyForecastItemResponse;
import com.weather.backend.dto.response.DailyForecastResponse;
import com.weather.backend.dto.response.HourlyForecastItemResponse;
import com.weather.backend.dto.response.HourlyForecastResponse;
import com.weather.backend.dto.response.WeatherAlertItemResponse;
import com.weather.backend.dto.response.WeatherAlertsResponse;
import com.weather.backend.dto.response.WeatherSummaryCurrentResponse;
import com.weather.backend.dto.response.WeatherSummaryResponse;
import com.weather.backend.model.GeocodingCoordinates;
import com.weather.backend.dto.openweather.OneCallResponse;
import com.weather.backend.model.Unit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private final GeocodingClient geocodingClient;
    private final OneCallClient oneCallClient;

    public WeatherService(GeocodingClient geocodingClient, OneCallClient oneCallClient) {
        this.geocodingClient = geocodingClient;
        this.oneCallClient = oneCallClient;
    }

    @Cacheable(
        value = CacheConfig.CURRENT_WEATHER_CACHE,
        key = "#city.trim().toLowerCase() + ':' + #units.getApiValue()"
    )
    public CurrentWeatherResponse getCurrentWeather(String city, Unit units) {
        WeatherLookup lookup = fetchWeather(city, units);
        WeatherSummaryCurrentResponse currentWeather = buildCurrentWeather(
            lookup.weather().current(),
            city
        );

        return new CurrentWeatherResponse(
            lookup.coordinates().cityName(),
            units.getApiValue(),
            currentWeather.temperature(),
            currentWeather.feelsLike(),
            currentWeather.humidity(),
            currentWeather.windSpeed(),
            currentWeather.windDirection(),
            currentWeather.uvIndex(),
            currentWeather.description()
        );
    }

    @Cacheable(
        value = CacheConfig.HOURLY_FORECAST_CACHE,
        key = "#city.trim().toLowerCase() + ':' + #hours + ':' + #units.getApiValue()"
    )
    public HourlyForecastResponse getHourlyForecast(String city, int hours, Unit units) {
        WeatherLookup lookup = fetchWeather(city, units);
        List<HourlyForecastItemResponse> forecast = buildForecast(
            lookup.weather().hourly(),
            hours,
            "Hourly forecast data is missing for city: " + city,
            this::toHourlyForecastItem
        );

        return new HourlyForecastResponse(
            lookup.coordinates().cityName(),
            units.getApiValue(),
            forecast
        );
    }

    @Cacheable(
        value = CacheConfig.DAILY_FORECAST_CACHE,
        key = "#city.trim().toLowerCase() + ':' + #days + ':' + #units.getApiValue()"
    )
    public DailyForecastResponse getDailyForecast(String city, int days, Unit units) {
        WeatherLookup lookup = fetchWeather(city, units);
        List<DailyForecastItemResponse> forecast = buildForecast(
            lookup.weather().daily(),
            days,
            "Daily forecast data is missing for city: " + city,
            this::toDailyForecastItem
        );

        return new DailyForecastResponse(
            lookup.coordinates().cityName(),
            units.getApiValue(),
            forecast
        );
    }

    @Cacheable(
        value = CacheConfig.WEATHER_ALERTS_CACHE,
        key = "#city.trim().toLowerCase()"
    )
    public WeatherAlertsResponse getWeatherAlerts(String city) {
        WeatherLookup lookup = fetchWeather(city, Unit.STANDARD);
        List<WeatherAlertItemResponse> responseAlerts = buildAlerts(lookup.weather().alerts());

        return new WeatherAlertsResponse(
            lookup.coordinates().cityName(),
            responseAlerts
        );
    }

    @Cacheable(
        value = CacheConfig.WEATHER_SUMMARY_CACHE,
        key = "#city.trim().toLowerCase() + ':' + #units.getApiValue()"
    )
    public WeatherSummaryResponse getWeatherSummary(String city, Unit units) {
        WeatherLookup lookup = fetchWeather(city, units);

        return new WeatherSummaryResponse(
            lookup.coordinates().cityName(),
            units.getApiValue(),
            buildCurrentWeather(lookup.weather().current(), city),
            buildForecast(
                lookup.weather().daily(),
                7,
                "Daily forecast data is missing for city: " + city,
                this::toDailyForecastItem
            ),
            buildAlerts(lookup.weather().alerts())
        );
    }

    private WeatherLookup fetchWeather(String city, Unit units) {
        GeocodingCoordinates coordinates = geocodingClient.resolveCity(city);
        OneCallResponse weather = oneCallClient.getWeather(
            coordinates.lat(),
            coordinates.lon(),
            units
        );
        return new WeatherLookup(coordinates, weather);
    }

    private String extractDescription(List<OneCallResponse.WeatherDescription> weatherDescriptions) {
        if (weatherDescriptions == null || weatherDescriptions.isEmpty()) {
            return "";
        }

        return weatherDescriptions.get(0).description();
    }

    private WeatherSummaryCurrentResponse buildCurrentWeather(
        OneCallResponse.CurrentWeather currentWeather,
        String city
    ) {
        if (currentWeather == null) {
            throw new IllegalStateException("Current weather data is missing for city: " + city);
        }

        return new WeatherSummaryCurrentResponse(
            currentWeather.temp(),
            currentWeather.feels_like(),
            currentWeather.humidity(),
            currentWeather.wind_speed(),
            currentWeather.wind_deg(),
            currentWeather.uvi(),
            extractDescription(currentWeather.weather())
        );
    }

    private HourlyForecastItemResponse toHourlyForecastItem(OneCallResponse.HourlyWeather hour) {
        return new HourlyForecastItemResponse(
            Instant.ofEpochSecond(hour.dt()),
            hour.temp(),
            hour.feels_like(),
            hour.humidity(),
            hour.wind_speed(),
            hour.wind_deg(),
            hour.pop(),
            extractDescription(hour.weather())
        );
    }

    private DailyForecastItemResponse toDailyForecastItem(OneCallResponse.DailyWeather day) {
        return new DailyForecastItemResponse(
            Instant.ofEpochSecond(day.dt()),
            day.temp().min(),
            day.temp().max(),
            day.pop(),
            extractDescription(day.weather())
        );
    }

    private WeatherAlertItemResponse toWeatherAlertItem(OneCallResponse.WeatherAlert alert) {
        return new WeatherAlertItemResponse(
            alert.sender_name(),
            alert.event(),
            Instant.ofEpochSecond(alert.start()),
            Instant.ofEpochSecond(alert.end()),
            alert.description()
        );
    }

    private List<WeatherAlertItemResponse> buildAlerts(List<OneCallResponse.WeatherAlert> alerts) {
        if (alerts == null) {
            return Collections.emptyList();
        }

        return alerts.stream()
            .map(this::toWeatherAlertItem)
            .collect(Collectors.toList());
    }

    private <T, R> List<R> buildForecast(
        List<T> source,
        int limit,
        String missingDataMessage,
        Function<T, R> mapper
    ) {
        if (source == null || source.isEmpty()) {
            throw new IllegalStateException(missingDataMessage);
        }

        return source.stream()
            .limit(limit)
            .map(mapper)
            .collect(Collectors.toList());
    }

    private record WeatherLookup(
        GeocodingCoordinates coordinates,
        OneCallResponse weather
    ) {
    }

}

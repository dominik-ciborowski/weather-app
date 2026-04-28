package com.weather.backend.exception;

import org.springframework.http.HttpStatusCode;

public class WeatherApiException extends RuntimeException {

    private final HttpStatusCode weatherStatus;
    private final String url;

    public WeatherApiException(HttpStatusCode weatherStatus, String message, String url) {
        super(message);
        this.weatherStatus = weatherStatus;
        this.url = url;
    }

    public HttpStatusCode getWeatherApiStatus() {
        return weatherStatus;
    }

    public String getUrl() {
        return url;
    }
}

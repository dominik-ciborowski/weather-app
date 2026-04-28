package com.weather.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "weather")
public record WeatherProperties(
    @NotBlank String apiKey,
    @NotBlank String baseUrl,
    @NotNull Duration timeout
) {
}

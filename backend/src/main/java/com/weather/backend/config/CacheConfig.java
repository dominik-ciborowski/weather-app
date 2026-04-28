package com.weather.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CURRENT_WEATHER_CACHE = "currentWeather";
    public static final String HOURLY_FORECAST_CACHE = "hourlyForecast";
    public static final String DAILY_FORECAST_CACHE = "dailyForecast";
    public static final String WEATHER_ALERTS_CACHE = "weatherAlerts";
    public static final String WEATHER_SUMMARY_CACHE = "weatherSummary";

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            CURRENT_WEATHER_CACHE,
            HOURLY_FORECAST_CACHE,
            DAILY_FORECAST_CACHE,
            WEATHER_ALERTS_CACHE,
            WEATHER_SUMMARY_CACHE
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10)));
        return cacheManager;
    }
}

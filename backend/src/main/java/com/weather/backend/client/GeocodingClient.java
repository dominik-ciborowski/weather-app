package com.weather.backend.client;

import com.weather.backend.config.WeatherProperties;
import com.weather.backend.exception.CityNotFoundException;
import com.weather.backend.exception.UpstreamUnavailableException;
import com.weather.backend.exception.WeatherApiException;
import com.weather.backend.model.GeocodingCoordinates;
import com.weather.backend.dto.openweather.GeocodingLocationResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

@Component
public class GeocodingClient {

    private final WebClient webClient;
    private final WeatherProperties weatherProperties;

    public GeocodingClient(WebClient webClient, WeatherProperties weatherProperties) {
        this.webClient = webClient;
        this.weatherProperties = weatherProperties;
    }

    public GeocodingCoordinates resolveCity(String city) {
        GeocodingLocationResponse[] response;
        String requestUrl = buildRequestUrl(city);
        try {
            response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/geo/1.0/direct")
                    .queryParam("q", city)
                    .queryParam("limit", 1)
                    .queryParam("appid", weatherProperties.apiKey())
                    .build())
                .retrieve()
                .bodyToMono(GeocodingLocationResponse[].class)
                .block();
        } catch (WebClientResponseException exception) {
            throw new WeatherApiException(
                HttpStatusCode.valueOf(exception.getStatusCode().value()),
                buildUpstreamMessage("geocoding", exception.getStatusCode()),
                requestUrl
            );
        } catch (WebClientRequestException exception) {
            throw new UpstreamUnavailableException(
                "OpenWeather geocoding API is unreachable",
                requestUrl,
                exception
            );
        }

        if (response == null || response.length == 0) {
            throw new CityNotFoundException(city);
        }

        GeocodingLocationResponse firstResult = Objects.requireNonNull(response[0]);
        return new GeocodingCoordinates(
            firstResult.name(),
            firstResult.lat(),
            firstResult.lon(),
            firstResult.country()
        );
    }

    private String buildUpstreamMessage(String apiName, HttpStatusCode statusCode) {
        return "OpenWeather " + apiName + " API failed with status " + statusCode.value();
    }

    private String buildRequestUrl(String city) {
        return weatherProperties.baseUrl()
            + "/geo/1.0/direct?q=" + city
            + "&limit=1&appid=??";
    }
}

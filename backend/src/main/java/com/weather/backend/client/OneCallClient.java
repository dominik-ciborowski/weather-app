package com.weather.backend.client;

import com.weather.backend.config.WeatherProperties;
import com.weather.backend.exception.UpstreamUnavailableException;
import com.weather.backend.exception.WeatherApiException;
import com.weather.backend.dto.openweather.OneCallResponse;
import com.weather.backend.model.Unit;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class OneCallClient {

    private final WebClient webClient;
    private final WeatherProperties weatherProperties;

    public OneCallClient(WebClient webClient, WeatherProperties weatherProperties) {
        this.webClient = webClient;
        this.weatherProperties = weatherProperties;
    }

    public OneCallResponse getWeather(double lat, double lon, Unit unit) {
        String requestUrl = buildRequestUrl(lat, lon, unit);
        try {
            return webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/data/3.0/onecall")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("units", unit.getApiValue())
                    .queryParam("appid", weatherProperties.apiKey())
                    .build())
                .retrieve()
                .bodyToMono(OneCallResponse.class)
                .block();
        } catch (WebClientResponseException exception) {
            throw new WeatherApiException(
                HttpStatusCode.valueOf(exception.getStatusCode().value()),
                buildUpstreamMessage(exception.getStatusCode()),
                requestUrl
            );
        } catch (WebClientRequestException exception) {
            throw new UpstreamUnavailableException(
                "OpenWeather One Call API is unreachable",
                requestUrl,
                exception
            );
        }
    }

    private String buildUpstreamMessage(HttpStatusCode statusCode) {
        return "OpenWeather One Call API failed with status " + statusCode.value();
    }

    private String buildRequestUrl(double lat, double lon, Unit unit) {
        return weatherProperties.baseUrl()
            + "/data/3.0/onecall?lat=" + lat
            + "&lon=" + lon
            + "&units=" + unit.getApiValue()
            + "&appid=[redacted]";
    }
}

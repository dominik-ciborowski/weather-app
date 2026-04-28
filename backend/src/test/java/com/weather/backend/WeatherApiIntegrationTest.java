package com.weather.backend;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.http.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeatherApiIntegrationTest {

    private static final WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort());

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!wireMock.isRunning()) {
            wireMock.start();
        }
        registry.add("weather.base-url", wireMock::baseUrl);
        registry.add("weather.api-key", () -> "test-api-key");
        registry.add("weather.timeout", () -> "5s");
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Test
    void summaryEndpointReturnsMappedOpenWeatherData() throws Exception {
        wireMock.stubFor(request(GET.name(), urlPathEqualTo("/geo/1.0/direct"))
            .withQueryParam("q", equalTo("Warsaw"))
            .withQueryParam("limit", equalTo("1"))
            .withQueryParam("appid", equalTo("test-api-key"))
            .willReturn(okJson("""
                [
                  {
                    "name": "Warsaw",
                    "lat": 52.2297,
                    "lon": 21.0122,
                    "country": "PL"
                  }
                ]
                """)));

        wireMock.stubFor(request(GET.name(), urlPathEqualTo("/data/3.0/onecall"))
            .withQueryParam("lat", equalTo("52.2297"))
            .withQueryParam("lon", equalTo("21.0122"))
            .withQueryParam("units", equalTo("metric"))
            .withQueryParam("appid", equalTo("test-api-key"))
            .willReturn(okJson("""
                {
                  "current": {
                    "temp": 18.5,
                    "feels_like": 17.2,
                    "humidity": 64,
                    "wind_speed": 5.6,
                    "wind_deg": 210,
                    "uvi": 3.9,
                    "weather": [
                      { "main": "Rain", "description": "light rain" }
                    ]
                  },
                  "daily": [
                    {
                      "dt": 1714825600,
                      "temp": { "min": 11.0, "max": 19.0 },
                      "pop": 0.35,
                      "weather": [
                        { "main": "Rain", "description": "showers" }
                      ]
                    },
                    {
                      "dt": 1714912000,
                      "temp": { "min": 10.5, "max": 20.2 },
                      "pop": 0.10,
                      "weather": [
                        { "main": "Clouds", "description": "broken clouds" }
                      ]
                    },
                    {
                      "dt": 1714998400,
                      "temp": { "min": 9.0, "max": 22.0 },
                      "pop": 0.00,
                      "weather": [
                        { "main": "Clear", "description": "clear sky" }
                      ]
                    }
                  ],
                  "alerts": [
                    {
                      "sender_name": "Government of Poland",
                      "event": "Flood Warning",
                      "start": 1714825600,
                      "end": 1714846000,
                      "description": "River levels are expected to rise."
                    }
                  ]
                }
                """)));

        HttpResponse<String> response = get("/api/weather/summary?city=Warsaw&units=metric");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"city\":\"Warsaw\"");
        assertThat(response.body()).contains("\"units\":\"metric\"");
        assertThat(response.body()).contains("\"description\":\"light rain\"");
        assertThat(response.body()).contains("\"event\":\"Flood Warning\"");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/geo/1.0/direct")));
        wireMock.verify(getRequestedFor(urlPathEqualTo("/data/3.0/onecall")));
    }

    @Test
    void currentEndpointReturnsNotFoundWhenCityCannotBeResolved() throws Exception {
        wireMock.stubFor(request(GET.name(), urlPathEqualTo("/geo/1.0/direct"))
            .withQueryParam("q", equalTo("Atlantis"))
            .willReturn(okJson("[]")));

        HttpResponse<String> response = get("/api/weather/current?city=Atlantis&units=metric");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("\"status\":404");
        assertThat(response.body()).contains("\"error\":\"City not found\"");
        assertThat(response.body()).contains("No coordinates found for city: 'Atlantis'");
    }

    @Test
    void currentEndpointReturnsBadGatewayWhenOpenWeatherReturnsUnauthorized() throws Exception {
        wireMock.stubFor(request(GET.name(), urlPathEqualTo("/geo/1.0/direct"))
            .withQueryParam("q", equalTo("Warsaw"))
            .willReturn(aResponse().withStatus(401)));

        HttpResponse<String> response = get("/api/weather/current?city=Warsaw&units=metric");

        assertThat(response.statusCode()).isEqualTo(502);
        assertThat(response.body()).contains("\"status\":502");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .GET()
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

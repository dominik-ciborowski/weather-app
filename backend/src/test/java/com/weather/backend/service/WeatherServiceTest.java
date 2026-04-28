package com.weather.backend.service;

import com.weather.backend.client.GeocodingClient;
import com.weather.backend.client.OneCallClient;
import com.weather.backend.dto.openweather.OneCallResponse;
import com.weather.backend.dto.response.CurrentWeatherResponse;
import com.weather.backend.dto.response.DailyForecastResponse;
import com.weather.backend.dto.response.WeatherAlertsResponse;
import com.weather.backend.dto.response.WeatherSummaryResponse;
import com.weather.backend.model.GeocodingCoordinates;
import com.weather.backend.model.Unit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private OneCallClient oneCallClient;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void getCurrentWeatherReturnsMappedResponse() {
        stubCoordinates("Warsaw");
        when(oneCallClient.getWeather(52.2297, 21.0122, Unit.METRIC)).thenReturn(weatherResponse());

        CurrentWeatherResponse response = weatherService.getCurrentWeather("Warsaw", Unit.METRIC);

        assertThat(response.city()).isEqualTo("Warsaw");
        assertThat(response.units()).isEqualTo("metric");
        assertThat(response.temperature()).isEqualTo(18.5);
        assertThat(response.feelsLike()).isEqualTo(17.2);
        assertThat(response.humidity()).isEqualTo(64);
        assertThat(response.windSpeed()).isEqualTo(5.6);
        assertThat(response.windDirection()).isEqualTo(210);
        assertThat(response.uvIndex()).isEqualTo(3.9);
        assertThat(response.description()).isEqualTo("light rain");
    }

    @Test
    void getDailyForecastLimitsResultToRequestedDays() {
        stubCoordinates("Warsaw");
        when(oneCallClient.getWeather(52.2297, 21.0122, Unit.METRIC)).thenReturn(weatherResponse());

        DailyForecastResponse response = weatherService.getDailyForecast("Warsaw", 2, Unit.METRIC);

        assertThat(response.city()).isEqualTo("Warsaw");
        assertThat(response.units()).isEqualTo("metric");
        assertThat(response.days()).hasSize(2);
        assertThat(response.days().get(0).minTemperature()).isEqualTo(11.0);
        assertThat(response.days().get(0).maxTemperature()).isEqualTo(19.0);
        assertThat(response.days().get(0).precipitationProbability()).isEqualTo(0.35);
        assertThat(response.days().get(1).date()).isEqualTo(Instant.ofEpochSecond(1_714_912_000L));
    }

    @Test
    void getWeatherAlertsReturnsEmptyListWhenNoAlertsPresent() {
        stubCoordinates("Warsaw");
        when(oneCallClient.getWeather(52.2297, 21.0122, Unit.STANDARD)).thenReturn(weatherWithoutAlerts());

        WeatherAlertsResponse response = weatherService.getWeatherAlerts("Warsaw");

        assertThat(response.city()).isEqualTo("Warsaw");
        assertThat(response.alerts()).isEmpty();
    }

    @Test
    void getWeatherSummaryCombinesCurrentForecastAndAlerts() {
        stubCoordinates("Warsaw");
        when(oneCallClient.getWeather(52.2297, 21.0122, Unit.IMPERIAL)).thenReturn(weatherResponse());

        WeatherSummaryResponse response = weatherService.getWeatherSummary("Warsaw", Unit.IMPERIAL);

        assertThat(response.city()).isEqualTo("Warsaw");
        assertThat(response.units()).isEqualTo("imperial");
        assertThat(response.current().temperature()).isEqualTo(18.5);
        assertThat(response.dailyForecast()).hasSize(3);
        assertThat(response.alerts()).hasSize(1);
        assertThat(response.alerts().get(0).event()).isEqualTo("Flood Warning");
    }

    @Test
    void getCurrentWeatherThrowsWhenUpstreamCurrentDataIsMissing() {
        stubCoordinates("Warsaw");
        when(oneCallClient.getWeather(52.2297, 21.0122, Unit.METRIC)).thenReturn(
            new OneCallResponse(null, List.of(), List.of(), List.of())
        );

        assertThatThrownBy(() -> weatherService.getCurrentWeather("Warsaw", Unit.METRIC))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Current weather data is missing for city: Warsaw");
    }

    private void stubCoordinates(String city) {
        when(geocodingClient.resolveCity(city)).thenReturn(new GeocodingCoordinates(
            "Warsaw",
            52.2297,
            21.0122,
            "PL"
        ));
    }

    private OneCallResponse weatherResponse() {
        return new OneCallResponse(
            new OneCallResponse.CurrentWeather(
                18.5,
                17.2,
                64,
                5.6,
                210,
                3.9,
                List.of(new OneCallResponse.WeatherDescription("Rain", "light rain"))
            ),
            List.of(),
            List.of(
                new OneCallResponse.DailyWeather(
                    1_714_825_600L,
                    new OneCallResponse.TemperatureRange(11.0, 19.0),
                    0.35,
                    List.of(new OneCallResponse.WeatherDescription("Rain", "showers"))
                ),
                new OneCallResponse.DailyWeather(
                    1_714_912_000L,
                    new OneCallResponse.TemperatureRange(10.5, 20.2),
                    0.10,
                    List.of(new OneCallResponse.WeatherDescription("Clouds", "broken clouds"))
                ),
                new OneCallResponse.DailyWeather(
                    1_714_998_400L,
                    new OneCallResponse.TemperatureRange(9.0, 22.0),
                    0.00,
                    List.of(new OneCallResponse.WeatherDescription("Clear", "clear sky"))
                )
            ),
            List.of(
                new OneCallResponse.WeatherAlert(
                    "Government of Poland",
                    "Flood Warning",
                    1_714_825_600L,
                    1_714_846_000L,
                    "River levels are expected to rise."
                )
            )
        );
    }

    private OneCallResponse weatherWithoutAlerts() {
        OneCallResponse response = weatherResponse();
        return new OneCallResponse(
            response.current(),
            response.hourly(),
            response.daily(),
            null
        );
    }
}

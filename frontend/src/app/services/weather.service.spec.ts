import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { WeatherService } from './weather.service';

describe('WeatherService', () => {
  let service: WeatherService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WeatherService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(WeatherService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('requests summary and 24-hour forecast for weather detail data', () => {
    const expectedSummary = {
      city: 'Warsaw',
      units: 'metric',
      current: {
        temperature: 18.5,
        feelsLike: 17.2,
        humidity: 64,
        windSpeed: 5.6,
        windDirection: 210,
        uvIndex: 3.9,
        description: 'light rain',
        icon: '10d'
      },
      dailyForecast: [],
      alerts: []
    };
    const expectedHourly = {
      city: 'Warsaw',
      units: 'metric',
      hours: []
    };

    service.getWeatherDetail('Warsaw', 'metric').subscribe((data) => {
      expect(data.summary).toEqual(expectedSummary);
      expect(data.hourly).toEqual(expectedHourly);
    });

    const summaryRequest = http.expectOne(
      'http://localhost:8080/api/weather/summary?city=Warsaw&units=metric'
    );
    expect(summaryRequest.request.method).toBe('GET');
    summaryRequest.flush(expectedSummary);

    const hourlyRequest = http.expectOne(
      'http://localhost:8080/api/weather/forecast/hourly?city=Warsaw&units=metric&hours=24'
    );
    expect(hourlyRequest.request.method).toBe('GET');
    hourlyRequest.flush(expectedHourly);
  });

  it('maps backend errors into UI-safe request errors', () => {
    service.getSummary('Atlantis', 'metric').subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error: unknown) => {
        expect(error).toEqual({
          status: 404,
          message: "No coordinates found for city: 'Atlantis'"
        });
      }
    });

    const request = http.expectOne(
      'http://localhost:8080/api/weather/summary?city=Atlantis&units=metric'
    );
    request.flush(
      {
        status: 404,
        error: 'City not found',
        message: "No coordinates found for city: 'Atlantis'",
        timestamp: '2026-04-29T00:00:00Z'
      },
      {
        status: 404,
        statusText: 'Not Found'
      }
    );
  });
});

import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, forkJoin, Observable, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import {
  HourlyForecastResponse,
  WeatherApiErrorResponse,
  WeatherRequestError,
  WeatherSummaryResponse,
  WeatherUnits
} from '../models/weather.models';

export interface WeatherDetailData {
  summary: WeatherSummaryResponse;
  hourly: HourlyForecastResponse;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  private readonly baseUrl = `${environment.weatherApiBaseUrl}/weather`;

  constructor(private readonly http: HttpClient) {}

  getWeatherDetail(city: string, units: WeatherUnits): Observable<WeatherDetailData> {
    return forkJoin({
      summary: this.getSummary(city, units),
      hourly: this.getHourlyForecast(city, units, 24)
    });
  }

  getSummary(city: string, units: WeatherUnits): Observable<WeatherSummaryResponse> {
    const params = new HttpParams().set('city', city).set('units', units);

    return this.http
      .get<WeatherSummaryResponse>(`${this.baseUrl}/summary`, { params })
      .pipe(catchError((error: HttpErrorResponse) => this.handleError(error)));
  }

  getHourlyForecast(city: string, units: WeatherUnits, hours: number): Observable<HourlyForecastResponse> {
    const params = new HttpParams()
      .set('city', city)
      .set('units', units)
      .set('hours', hours);

    return this.http
      .get<HourlyForecastResponse>(`${this.baseUrl}/forecast/hourly`, { params })
      .pipe(catchError((error: HttpErrorResponse) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    const response = this.isWeatherApiError(error.error) ? error.error : null;
    const requestError: WeatherRequestError = {
      status: error.status,
      message: response?.message ?? this.fallbackMessage(error.status)
    };

    return throwError(() => requestError);
  }

  private fallbackMessage(status: number): string {
    if (status === 404) {
      return 'We could not find that city. Check the spelling and try again.';
    }

    if (status === 0) {
      return 'Could not reach the weather backend. Make sure it is running on localhost:8080.';
    }

    return 'Weather data could not be loaded right now.';
  }

  private isWeatherApiError(value: unknown): value is WeatherApiErrorResponse {
    return (
      typeof value === 'object' &&
      value !== null &&
      'message' in value &&
      typeof value.message === 'string'
    );
  }
}

export type WeatherUnits = 'metric' | 'imperial' | 'standard';

export interface WeatherSummaryResponse {
  city: string;
  units: WeatherUnits;
  current: WeatherSummaryCurrentResponse;
  dailyForecast: DailyForecastItemResponse[];
  alerts: WeatherAlertItemResponse[];
}

export interface WeatherSummaryCurrentResponse {
  temperature: number;
  feelsLike: number;
  humidity: number;
  windSpeed: number;
  windDirection: number;
  uvIndex: number;
  description: string;
  icon: string;
}

export interface DailyForecastItemResponse {
  date: string;
  minTemperature: number;
  maxTemperature: number;
  precipitationProbability: number;
  description: string;
  icon: string;
}

export interface HourlyForecastResponse {
  city: string;
  units: WeatherUnits;
  hours: HourlyForecastItemResponse[];
}

export interface HourlyForecastItemResponse {
  time: string;
  temperature: number;
  feelsLike: number;
  humidity: number;
  windSpeed: number;
  windDirection: number;
  precipitationProbability: number;
  description: string;
  icon: string;
}

export interface WeatherAlertItemResponse {
  source: string;
  event: string;
  start: string;
  end: string;
  description: string;
}

export interface WeatherApiErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}

export interface WeatherRequestError {
  status: number;
  message: string;
}

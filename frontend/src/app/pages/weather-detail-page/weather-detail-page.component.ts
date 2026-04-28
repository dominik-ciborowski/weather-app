import { DatePipe, DecimalPipe, PercentPipe, TitleCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { catchError, combineLatest, EMPTY, map, switchMap, tap } from 'rxjs';

import { WeatherDetailData, WeatherService } from '../../services/weather.service';
import { WeatherRequestError, WeatherUnits } from '../../models/weather.models';

const WEATHER_ICON_BASE_URL = 'https://openweathermap.org/img/wn';

interface UnitsOption {
  label: string;
  value: WeatherUnits;
}

@Component({
  selector: 'app-weather-detail-page',
  imports: [BaseChartDirective, DatePipe, DecimalPipe, PercentPipe, RouterLink, TitleCasePipe],
  templateUrl: './weather-detail-page.component.html',
  styleUrl: './weather-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WeatherDetailPageComponent {
  protected readonly unitsOptions: UnitsOption[] = [
    { label: 'Metric', value: 'metric' },
    { label: 'Imperial', value: 'imperial' },
    { label: 'Standard', value: 'standard' }
  ];

  protected readonly city = signal('');
  protected readonly units = signal<WeatherUnits>('metric');
  protected readonly weather = signal<WeatherDetailData | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<WeatherRequestError | null>(null);
  protected readonly chartData = computed<ChartConfiguration<'line'>['data']>(() => {
    const hours = this.weather()?.hourly.hours.slice(0, 24) ?? [];

    return {
      labels: hours.map((hour) =>
        new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit' }).format(new Date(hour.time))
      ),
      datasets: [
        {
          data: hours.map((hour) => hour.temperature),
          label: 'Temperature',
          borderColor: '#1f6f62',
          backgroundColor: 'rgba(31, 111, 98, 0.16)',
          fill: true,
          pointRadius: 2,
          tension: 0.35
        }
      ]
    };
  });

  protected readonly chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        }
      },
      y: {
        ticks: {
          callback: (value) => `${value}${this.temperatureSuffix()}`
        }
      }
    }
  };

  private readonly route = inject(ActivatedRoute);
  private readonly weatherService = inject(WeatherService);

  constructor() {
    combineLatest([
      this.route.paramMap.pipe(map((params) => params.get('city') ?? '')),
      toObservable(this.units)
    ])
      .pipe(
        tap(([city]) => {
          this.city.set(city);
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap(([city, units]) =>
          this.weatherService.getWeatherDetail(city, units).pipe(
            catchError((error: WeatherRequestError) => {
              this.weather.set(null);
              this.error.set(error);
              this.loading.set(false);
              return EMPTY;
            })
          )
        ),
        takeUntilDestroyed()
      )
      .subscribe((weather) => {
        this.weather.set(weather);
        this.loading.set(false);
      });
  }

  protected setUnits(units: WeatherUnits): void {
    this.units.set(units);
  }

  protected weatherIconUrl(icon: string): string {
    return `${WEATHER_ICON_BASE_URL}/${icon}@2x.png`;
  }

  protected temperatureSuffix(): string {
    if (this.units() === 'standard') {
      return 'K';
    }

    return this.units() === 'imperial' ? '°F' : '°C';
  }

  protected windSpeedSuffix(): string {
    return this.units() === 'imperial' ? 'mph' : 'm/s';
  }

  protected windDirectionLabel(degrees: number): string {
    const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
    const index = Math.round(degrees / 45) % directions.length;
    return directions[index];
  }
}

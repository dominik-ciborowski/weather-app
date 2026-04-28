import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

const RECENT_CITIES_STORAGE_KEY = 'weather-dashboard:recent-cities';
const MAX_RECENT_CITIES = 8;

@Component({
  selector: 'app-home-page',
  imports: [FormsModule],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePageComponent {
  protected city = '';
  protected readonly recentCities = signal<string[]>(this.loadRecentCities());

  constructor(private readonly router: Router) {}

  protected submitSearch(): void {
    const city = this.normalizeCity(this.city);

    if (!city) {
      return;
    }

    this.saveRecentCity(city);
    void this.router.navigate(['/weather', city]);
  }

  protected searchRecentCity(city: string): void {
    this.saveRecentCity(city);
    void this.router.navigate(['/weather', city]);
  }

  protected removeRecentCity(city: string): void {
    const nextCities = this.recentCities().filter(
      (recentCity) => recentCity.toLocaleLowerCase() !== city.toLocaleLowerCase()
    );

    this.recentCities.set(nextCities);
    this.persistRecentCities(nextCities);
  }

  private saveRecentCity(city: string): void {
    const nextCities = [
      city,
      ...this.recentCities().filter(
        (recentCity) => recentCity.toLocaleLowerCase() !== city.toLocaleLowerCase()
      )
    ].slice(0, MAX_RECENT_CITIES);

    this.recentCities.set(nextCities);
    this.persistRecentCities(nextCities);
    this.city = '';
  }

  private loadRecentCities(): string[] {
    const rawCities = localStorage.getItem(RECENT_CITIES_STORAGE_KEY);

    if (!rawCities) {
      return [];
    }

    try {
      const cities: unknown = JSON.parse(rawCities);

      if (!Array.isArray(cities)) {
        return [];
      }

      return cities
        .filter((city): city is string => typeof city === 'string')
        .map((city) => this.normalizeCity(city))
        .filter((city) => city.length > 0)
        .slice(0, MAX_RECENT_CITIES);
    } catch {
      return [];
    }
  }

  private persistRecentCities(cities: string[]): void {
    localStorage.setItem(RECENT_CITIES_STORAGE_KEY, JSON.stringify(cities));
  }

  private normalizeCity(city: string): string {
    return city.trim().replace(/\s+/g, ' ');
  }
}

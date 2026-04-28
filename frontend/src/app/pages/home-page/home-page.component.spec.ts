import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';

import { HomePageComponent } from './home-page.component';

const RECENT_CITIES_STORAGE_KEY = 'weather-dashboard:recent-cities';

@Component({
  template: ''
})
class TestWeatherDetailComponent {}

describe('HomePageComponent', () => {
  let fixture: ComponentFixture<HomePageComponent>;
  let router: Router;
  let storage: Storage;

  beforeEach(async () => {
    storage = createMemoryStorage();
    vi.stubGlobal('localStorage', storage);

    await TestBed.configureTestingModule({
      imports: [HomePageComponent],
      providers: [
        provideRouter([
          {
            path: 'weather/:city',
            component: TestWeatherDetailComponent
          }
        ])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomePageComponent);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('navigates to the weather page and saves the searched city', async () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const input = fixture.debugElement.query(By.css('input')).nativeElement as HTMLInputElement;
    const form = fixture.debugElement.query(By.css('form')).nativeElement as HTMLFormElement;

    input.value = '  Warsaw   Poland ';
    input.dispatchEvent(new Event('input'));
    form.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    expect(navigateSpy).toHaveBeenCalledWith(['/weather', 'Warsaw Poland']);
    expect(JSON.parse(storage.getItem(RECENT_CITIES_STORAGE_KEY) ?? '[]')).toEqual([
      'Warsaw Poland'
    ]);
  });

  it('renders saved recent cities and removes one', () => {
    storage.setItem(RECENT_CITIES_STORAGE_KEY, JSON.stringify(['Warsaw', 'Tokyo']));
    fixture = TestBed.createComponent(HomePageComponent);
    fixture.detectChanges();

    const cityButtons = fixture.debugElement.queryAll(By.css('.recent-city'));
    expect(cityButtons.map((button) => button.nativeElement.textContent.trim())).toEqual([
      'Warsaw',
      'Tokyo'
    ]);

    const removeButton = fixture.debugElement.query(By.css('.remove-city')).nativeElement as HTMLButtonElement;
    removeButton.click();
    fixture.detectChanges();

    expect(JSON.parse(storage.getItem(RECENT_CITIES_STORAGE_KEY) ?? '[]')).toEqual(['Tokyo']);
    expect(fixture.nativeElement.textContent).not.toContain('Warsaw');
  });
});

function createMemoryStorage(): Storage {
  const values = new Map<string, string>();

  return {
    get length(): number {
      return values.size;
    },
    clear(): void {
      values.clear();
    },
    getItem(key: string): string | null {
      return values.get(key) ?? null;
    },
    key(index: number): string | null {
      return Array.from(values.keys())[index] ?? null;
    },
    removeItem(key: string): void {
      values.delete(key);
    },
    setItem(key: string, value: string): void {
      values.set(key, value);
    }
  };
}

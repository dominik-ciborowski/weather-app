import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { WeatherDetailPageComponent } from './pages/weather-detail-page/weather-detail-page.component';

export const routes: Routes = [
  {
    path: '',
    component: HomePageComponent
  },
  {
    path: 'weather/:city',
    component: WeatherDetailPageComponent
  }
];

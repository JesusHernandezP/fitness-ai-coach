import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface BodyMetricsProgressDto {
  date: string;
  weight: number;
}

export interface WeeklySummaryDto {
  periodLabel: string;
  caloriesConsumed: number;
  caloriesBurned: number;
  steps: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api';

  getWeightProgress(): Observable<BodyMetricsProgressDto[]> {
    return this.http.get<BodyMetricsProgressDto[]>(`${this.baseUrl}/body-metrics/progress`);
  }

  getWeeklySummary(): Observable<WeeklySummaryDto> {
    return this.http.get<WeeklySummaryDto>(`${this.baseUrl}/daily-logs/summary/week`);
  }
}

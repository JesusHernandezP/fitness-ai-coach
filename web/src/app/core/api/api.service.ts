import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface BodyMetricsProgressDto {
  date: string;
  weight: number;
}

export interface WeeklySummaryDto {
  weekStart: string;
  weekEnd: string;
  summary: string;
  recommendation: string;
}

export interface AIChatMessageDto {
  role: 'USER' | 'ASSISTANT';
  message: string;
  createdAt: string;
}

export interface SendChatMessageRequestDto {
  message: string;
}

export interface SendChatMessageResponseDto {
  reply: string;
  loggedSummary?: string[];
}

export interface MetabolicProfileDto {
  userId?: string;
  age: number | null;
  heightCm: number | null;
  weightKg: number | null;
  sex: 'MALE' | 'FEMALE' | '' | null;
  activityLevel: 'SEDENTARY' | 'LIGHT' | 'MODERATE' | 'ACTIVE' | 'VERY_ACTIVE' | '' | null;
  dietType: 'STANDARD' | 'KETO' | 'VEGETARIAN' | '' | null;
  goalType: 'LOSE_WEIGHT' | 'BUILD_MUSCLE' | 'MAINTAIN' | '' | null;
  targetCalories?: number | null;
  targetProtein?: number | null;
  targetCarbs?: number | null;
  targetFat?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getWeightProgress(): Observable<BodyMetricsProgressDto[]> {
    return this.http.get<BodyMetricsProgressDto[]>(`${this.baseUrl}/body-metrics/progress`);
  }

  getWeeklySummary(): Observable<WeeklySummaryDto> {
    return this.http.get<WeeklySummaryDto>(`${this.baseUrl}/ai-coach/weekly-summary`);
  }

  getChatHistory(): Observable<AIChatMessageDto[]> {
    return this.http.get<AIChatMessageDto[]>(`${this.baseUrl}/ai-chat/history`);
  }

  sendChatMessage(payload: SendChatMessageRequestDto): Observable<SendChatMessageResponseDto> {
    return this.http.post<SendChatMessageResponseDto>(`${this.baseUrl}/ai-chat/message`, payload);
  }

  getProfile(): Observable<MetabolicProfileDto> {
    return this.http.get<MetabolicProfileDto>(`${this.baseUrl}/users/profile`);
  }

  updateProfile(payload: MetabolicProfileDto): Observable<MetabolicProfileDto> {
    return this.http.put<MetabolicProfileDto>(`${this.baseUrl}/users/profile`, payload);
  }
}

import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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
    return this.http.get<WeeklySummaryDto>(`${this.baseUrl}/ai-coach/weekly-summary`);
  }

  getChatHistory(): Observable<AIChatMessageDto[]> {
    return this.http.get<AIChatMessageDto[]>(`${this.baseUrl}/ai-chat/history`);
  }

  sendChatMessage(payload: SendChatMessageRequestDto): Observable<SendChatMessageResponseDto> {
    return this.http.post<SendChatMessageResponseDto>(`${this.baseUrl}/ai-chat/message`, payload);
  }
}

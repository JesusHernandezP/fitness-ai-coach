import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface ApiResponse<T> {
  timestamp?: string;
  status?: number;
  data: T;
}

interface DailyLogDto {
  id: string;
  date: string;
  steps: number;
  caloriesConsumed: number;
  caloriesBurned: number;
  userId: string;
}

interface AICoachingResponseDto {
  analysis?: unknown;
  advice: string;
}

interface UserDto {
  id: string;
  name: string;
  email: string;
  age: number | null;
  heightCm: number | null;
  weightKg: number | null;
  sex?: 'MALE' | 'FEMALE' | null;
  activityLevel?: 'SEDENTARY' | 'LIGHT' | 'MODERATE' | 'ACTIVE' | 'VERY_ACTIVE' | null;
}

interface GoalDto {
  id: string;
  userId: string;
  goalType: 'LOSE_WEIGHT' | 'BUILD_MUSCLE' | 'MAINTAIN' | null;
  targetWeight?: number | null;
  targetCalories?: number | null;
  targetProtein?: number | null;
  targetCarbs?: number | null;
  targetFat?: number | null;
}

type MaybeWrapped<T> = ApiResponse<T> | T;

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
  private readonly chatStorageKey = 'fitness-ai-coach.chat-history';

  getWeightProgress(): Observable<BodyMetricsProgressDto[]> {
    return this.http.get<MaybeWrapped<BodyMetricsProgressDto[]>>(`${this.baseUrl}/body-metrics/progress`).pipe(
      map((response) => this.unwrap(response))
    );
  }

  getWeeklySummary(): Observable<WeeklySummaryDto> {
    return this.getTodayDailyLog().pipe(
      switchMap((dailyLog) =>
        this.http.get<MaybeWrapped<AICoachingResponseDto>>(`${this.baseUrl}/ai-coach/daily-log/${dailyLog.id}`).pipe(
          map((response) => this.unwrap(response)),
          map((coaching) => ({
            weekStart: dailyLog.date,
            weekEnd: dailyLog.date,
            summary: this.toSummaryText(coaching.analysis),
            recommendation: coaching.advice?.trim() || 'No recommendation available yet.'
          }))
        )
      )
    );
  }

  getChatHistory(): Observable<AIChatMessageDto[]> {
    return of(this.readChatHistory());
  }

  sendChatMessage(payload: SendChatMessageRequestDto): Observable<SendChatMessageResponseDto> {
    const userMessage: AIChatMessageDto = {
      role: 'USER',
      message: payload.message,
      createdAt: new Date().toISOString()
    };

    return this.http.post<SendChatMessageResponseDto>(`${this.baseUrl}/ai-chat/message`, payload).pipe(
      tap((response) => {
        const history = this.readChatHistory();
        history.push(
          userMessage,
          {
            role: 'ASSISTANT',
            message: response.reply,
            createdAt: new Date().toISOString()
          }
        );
        this.writeChatHistory(history);
      })
    );
  }

  getProfile(): Observable<MetabolicProfileDto> {
    return this.getTodayDailyLog().pipe(
      switchMap((dailyLog) =>
        forkJoin({
          user: this.http.get<MaybeWrapped<UserDto>>(`${this.baseUrl}/users/${dailyLog.userId}`).pipe(
            map((response) => this.unwrap(response))
          ),
          goals: this.http.get<MaybeWrapped<GoalDto[]>>(`${this.baseUrl}/goals`).pipe(
            map((response) => this.unwrap(response))
          )
        })
      ),
      map(({ user, goals }) => {
        const currentGoal = goals.find((goal) => goal.userId === user.id) ?? null;

        return {
          userId: user.id,
          age: user.age ?? null,
          heightCm: user.heightCm ?? null,
          weightKg: user.weightKg ?? null,
          sex: user.sex ?? null,
          activityLevel: user.activityLevel ?? null,
          dietType: null,
          goalType: currentGoal?.goalType ?? null,
          targetCalories: currentGoal?.targetCalories ?? null,
          targetProtein: currentGoal?.targetProtein ?? null,
          targetCarbs: currentGoal?.targetCarbs ?? null,
          targetFat: currentGoal?.targetFat ?? null
        };
      })
    );
  }

  updateProfile(payload: MetabolicProfileDto): Observable<MetabolicProfileDto> {
    return this.getTodayDailyLog().pipe(
      switchMap((dailyLog) =>
        this.http.put<MaybeWrapped<UserDto>>(`${this.baseUrl}/users/${dailyLog.userId}`, {
          age: payload.age,
          heightCm: payload.heightCm,
          sex: payload.sex || null,
          activityLevel: payload.activityLevel || null
        })
      ),
      switchMap(() => this.getProfile())
    );
  }

  private getTodayDailyLog(): Observable<DailyLogDto> {
    return this.http.get<MaybeWrapped<DailyLogDto>>(`${this.baseUrl}/daily-logs/today`).pipe(
      map((response) => this.unwrap(response))
    );
  }

  private unwrap<T>(response: MaybeWrapped<T>): T {
    if (
      response !== null &&
      typeof response === 'object' &&
      'data' in response
    ) {
      return (response as ApiResponse<T>).data;
    }

    return response as T;
  }

  private toSummaryText(analysis: unknown): string {
    if (typeof analysis === 'string' && analysis.trim()) {
      return analysis.trim();
    }

    if (analysis && typeof analysis === 'object') {
      return 'Daily coaching context generated from your latest log.';
    }

    return 'No analysis available yet.';
  }

  private readChatHistory(): AIChatMessageDto[] {
    const rawHistory = localStorage.getItem(this.chatStorageKey);
    if (!rawHistory) {
      return [];
    }

    try {
      const parsed = JSON.parse(rawHistory) as AIChatMessageDto[];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private writeChatHistory(messages: AIChatMessageDto[]): void {
    localStorage.setItem(this.chatStorageKey, JSON.stringify(messages));
  }
}

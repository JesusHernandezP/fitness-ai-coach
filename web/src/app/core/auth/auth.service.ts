import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface LoginRequestDto {
  email: string;
  password: string;
}

interface LoginResponseDto {
  token: string;
}

interface ApiResponseDto<T> {
  data: T;
}

interface RegisterRequestDto {
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenKey = 'fitness-ai-coach.token';
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  login(payload: LoginRequestDto): Observable<LoginResponseDto> {
    return this.http.post<ApiResponseDto<LoginResponseDto>>(`${this.baseUrl}/login`, payload).pipe(
      tap((response) => localStorage.setItem(this.tokenKey, response.data.token)),
      map((response) => response.data)
    );
  }

  register(payload: RegisterRequestDto): Observable<LoginResponseDto> {
    const name = payload.email.split('@')[0]?.trim() || 'web-user';

    return this.http.post<ApiResponseDto<unknown>>(`${this.baseUrl}/register`, {
      name,
      email: payload.email,
      password: payload.password,
      age: 30,
      heightCm: 170,
      weightKg: 70
    }).pipe(
      switchMap(() => this.login(payload))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    void this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return Boolean(this.getToken());
  }
}

import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="login">
      <div class="login__hero">
        <img src="/logo-fitness-ai-coach.png" alt="Fitness AI Coach" class="login__logo" />
        <p class="login__eyebrow">Fitness AI Coach</p>
        <h1>{{ isRegisterMode ? 'Crea tu cuenta' : 'Bienvenido de vuelta' }}</h1>
        <p class="login__slogan">Tu coach de bolsillo</p>
        <p class="login__copy">
          {{ isRegisterMode
            ? 'Empieza con email y contraseña. Tu progreso y coaching quedarán listos en web y Android.'
            : 'Entra para continuar tu progreso, tu chat y tus recomendaciones sincronizadas.' }}
        </p>
      </div>

      <div class="login__panel">
        <form class="login__form" (ngSubmit)="submit()">
          <label>
            <span>Email</span>
            <input type="email" name="email" [(ngModel)]="email" [disabled]="isLoading" required />
          </label>

          <label>
            <span>Password</span>
            <input type="password" name="password" [(ngModel)]="password" [disabled]="isLoading" required />
          </label>

          @if (errorMessage) {
            <p class="login__error">{{ errorMessage }}</p>
          }

          <button type="submit" [disabled]="isLoading || !email.trim() || !password.trim()">
            {{ isLoading ? (isRegisterMode ? 'Creating account...' : 'Signing in...') : (isRegisterMode ? 'Crear usuario' : 'Entrar') }}
          </button>

          <button type="button" class="login__ghost" (click)="toggleMode()" [disabled]="isLoading">
            {{ isRegisterMode ? 'Ya tengo cuenta' : 'Crear usuario' }}
          </button>
        </form>
      </div>
    </section>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background:
        radial-gradient(circle at top right, rgba(255, 224, 30, 0.14), transparent 22rem),
        linear-gradient(180deg, #111318 0%, #0b0c10 100%);
      color: #ffffff;
      font-family: Inter, Roboto, system-ui, sans-serif;
    }

    .login {
      min-height: 100vh;
      display: grid;
      grid-template-columns: minmax(320px, 1.1fr) minmax(320px, 0.9fr);
      gap: 28px;
      align-items: center;
      padding: 32px;
    }

    .login__hero,
    .login__panel {
      background: rgba(30, 30, 30, 0.92);
      border: 1px solid #2a2a2a;
      border-radius: 28px;
      box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
    }

    .login__hero {
      padding: 36px;
      display: grid;
      gap: 12px;
      align-content: center;
      min-height: 520px;
    }

    .login__panel {
      padding: 32px;
    }

    .login__logo {
      width: min(100%, 320px);
      justify-self: center;
      filter: drop-shadow(0 24px 40px rgba(255, 224, 30, 0.12));
      margin-bottom: 8px;
    }

    .login__eyebrow {
      margin: 0;
      color: #ffe01e;
      text-transform: uppercase;
      letter-spacing: 0.16em;
      font-size: 0.78rem;
      font-weight: 700;
    }

    h1 {
      margin: 0;
      font-size: 2.6rem;
      line-height: 1;
    }

    .login__slogan {
      margin: 0;
      color: #ffe01e;
      font-size: 1.2rem;
      font-weight: 700;
    }

    .login__copy {
      margin: 0;
      max-width: 34rem;
      color: #a0a0a0;
      line-height: 1.6;
      font-size: 1rem;
    }

    .login__form {
      display: grid;
      gap: 16px;
    }

    label {
      display: grid;
      gap: 8px;
      color: #a0a0a0;
      font-size: 0.95rem;
    }

    input {
      min-height: 56px;
      border-radius: 16px;
      border: 1px solid #2a2a2a;
      background: #121212;
      color: #ffffff;
      padding: 0 16px;
      font: inherit;
      outline: none;
    }

    input:focus {
      border-color: #ffe01e;
    }

    button {
      min-height: 54px;
      border: none;
      border-radius: 16px;
      background: #ffe01e;
      color: #000000;
      font: inherit;
      font-weight: 700;
      cursor: pointer;
    }

    .login__ghost {
      background: transparent;
      border: 1px solid #2a2a2a;
      color: #ffffff;
    }

    button:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .login__error {
      margin: 0;
      color: #ef4444;
    }

    @media (max-width: 980px) {
      .login {
        grid-template-columns: 1fr;
      }

      .login__hero {
        min-height: auto;
      }
    }
  `]
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected email = '';
  protected password = '';
  protected isLoading = false;
  protected errorMessage = '';
  protected isRegisterMode = false;

  protected toggleMode(): void {
    if (this.isLoading) {
      return;
    }

    this.isRegisterMode = !this.isRegisterMode;
    this.errorMessage = '';
  }

  protected submit(): void {
    if (this.isLoading) {
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    const authRequest = this.isRegisterMode
      ? this.authService.register({
          email: this.email.trim(),
          password: this.password.trim()
        })
      : this.authService.login({
          email: this.email.trim(),
          password: this.password.trim()
        });

    authRequest.pipe(
      timeout(10000)
    ).subscribe({
      next: () => {
        this.isLoading = false;
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = this.isRegisterMode
          ? 'Unable to create the account. Check the email, password and backend connection.'
          : 'Unable to sign in. Check your credentials and backend connection.';
      }
    });
  }
}

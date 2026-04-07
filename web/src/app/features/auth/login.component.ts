import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="login">
      <div class="login__panel">
        <p class="login__eyebrow">Fitness AI Coach</p>
        <h1>Sign in</h1>
        <p class="login__copy">Use the same account as the Android app to keep your chat, progress and coaching in sync.</p>

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
            {{ isLoading ? 'Signing in...' : 'Enter dashboard' }}
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
      place-items: center;
      padding: 24px;
    }

    .login__panel {
      width: min(100%, 440px);
      background: rgba(30, 30, 30, 0.94);
      border: 1px solid #2a2a2a;
      border-radius: 24px;
      padding: 32px;
      box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
    }

    .login__eyebrow {
      margin: 0 0 12px;
      color: #ffe01e;
      text-transform: uppercase;
      letter-spacing: 0.16em;
      font-size: 0.75rem;
      font-weight: 700;
    }

    h1 {
      margin: 0 0 8px;
      font-size: 2rem;
    }

    .login__copy {
      margin: 0 0 24px;
      color: #a0a0a0;
      line-height: 1.5;
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
      min-height: 52px;
      border-radius: 14px;
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
      min-height: 52px;
      border: none;
      border-radius: 14px;
      background: #ffe01e;
      color: #000000;
      font: inherit;
      font-weight: 700;
      cursor: pointer;
    }

    button:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .login__error {
      margin: 0;
      color: #ef4444;
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

  protected submit(): void {
    if (this.isLoading) {
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.authService.login({
      email: this.email.trim(),
      password: this.password.trim()
    }).subscribe({
      next: () => {
        this.isLoading = false;
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Unable to sign in. Check your credentials and backend connection.';
      }
    });
  }
}

import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { timeout } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { SpinnerComponent } from '../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, SpinnerComponent],
  template: `
    <section class="login">
      <div class="login__card">
        <div class="login__header">
          <img src="/logo-fitness-ai-coach.png" alt="Fitness AI Coach" class="login__logo" />
          <p class="login__eyebrow">Fitness AI Coach</p>
          <h1>{{ isRegisterMode ? 'Crea tu cuenta' : 'Bienvenido de vuelta' }}</h1>
          <p class="login__slogan">Tu coach de bolsillo</p>
          <p class="login__copy">
            {{
              isRegisterMode
                ? 'Empieza con correo y contrasena. Tu progreso y coaching se mantienen alineados entre web y Android.'
                : 'Inicia sesion para continuar tu progreso, chat y coaching sincronizado.'
            }}
          </p>
        </div>

        <form class="login__form" (ngSubmit)="submit()">
          <label>
            <span>Correo</span>
            <input type="email" name="email" [(ngModel)]="email" [disabled]="isLoading" required />
          </label>

          <label>
            <span>Contrasena</span>
            <input
              type="password"
              name="password"
              [(ngModel)]="password"
              [disabled]="isLoading"
              required
            />
          </label>

          @if (errorMessage) {
            <p class="login__error">{{ errorMessage }}</p>
          }

          <button type="submit" [disabled]="isLoading || !email.trim() || !password.trim()">
            @if (isLoading) {
              <span class="login__button-content">
                <app-spinner size="small" />
                <span>{{ isRegisterMode ? 'Creando cuenta...' : 'Iniciando sesion...' }}</span>
              </span>
            } @else {
              {{ isRegisterMode ? 'Crear cuenta' : 'Entrar' }}
            }
          </button>

          <button type="button" class="login__ghost" (click)="toggleMode()" [disabled]="isLoading">
            {{ isRegisterMode ? 'Ya tengo una cuenta' : 'Crear cuenta' }}
          </button>
        </form>
      </div>
    </section>
  `,
  styles: [
    `
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
        padding: var(--spacing-section);
      }

      .login__card {
        background: rgba(30, 30, 30, 0.92);
        border: 1px solid #2a2a2a;
        border-radius: 28px;
        box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
        width: min(520px, 92vw);
        padding: var(--spacing-section);
        display: grid;
        gap: var(--spacing-section);
      }

      .login__header {
        display: grid;
        gap: 10px;
        text-align: center;
      }

      .login__logo {
        height: 90px;
        width: auto;
        max-width: min(100%, 320px);
        justify-self: center;
        object-fit: contain;
        filter: drop-shadow(0 24px 40px rgba(255, 224, 30, 0.12));
        margin-bottom: 8px;
      }

      .login__eyebrow {
        margin: 0;
        color: #ffe01e;
        text-transform: uppercase;
        letter-spacing: 0.16em;
        font-size: var(--font-size-label);
        font-weight: 700;
      }

      h1 {
        margin: 0;
        font-size: var(--font-size-page-title);
        font-weight: var(--font-weight-semibold);
        line-height: 1.4;
      }

      .login__slogan {
        margin: 0;
        color: #ffe01e;
        font-size: 1.2rem;
        font-weight: 700;
      }

      .login__copy {
        margin: 0;
        color: #a0a0a0;
        line-height: var(--line-height-relaxed);
        font-size: var(--font-size-body);
      }

      .login__form {
        display: grid;
        gap: 16px;
      }

      label {
        display: grid;
        gap: 8px;
        color: #a0a0a0;
        font-size: var(--font-size-body);
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
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
      }

      .login__button-content {
        display: flex;
        align-items: center;
        gap: 10px;
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
        font-size: var(--font-size-body);
      }

      @media (max-width: 600px) {
        .login__card {
          padding: 28px 22px;
        }

        h1 {
          font-size: 2.1rem;
        }
      }
    `,
  ],
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
          password: this.password.trim(),
        })
      : this.authService.login({
          email: this.email.trim(),
          password: this.password.trim(),
        });

    authRequest.pipe(timeout(10000)).subscribe({
      next: () => {
        this.isLoading = false;
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = this.isRegisterMode
          ? 'No se pudo crear la cuenta. Revisa el correo, la contrasena y la conexion al backend.'
          : 'No se pudo iniciar sesion. Revisa tus credenciales y la conexion al backend.';
      },
    });
  }
}

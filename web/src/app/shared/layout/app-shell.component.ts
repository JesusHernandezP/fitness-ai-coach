import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="shell">
      @if (showShell()) {
        <header class="shell__header">
          <div class="shell__brand">
            <img src="/logo-fitness-ai-coach.png" alt="Fitness AI Coach" class="shell__logo" />
            <div>
              <p class="shell__eyebrow">Fitness AI Coach</p>
              <h1>{{ currentSectionTitle() }}</h1>
            </div>
          </div>

          <div class="shell__actions">
            <a routerLink="/chat" class="shell__cta">Open chat</a>
            <button type="button" class="shell__logout" (click)="logout()">Log out</button>
          </div>
        </header>

        <div class="shell__body">
          <nav class="shell__nav" aria-label="Primary">
            <a routerLink="/dashboard" routerLinkActive="is-active" [routerLinkActiveOptions]="{ exact: true }">
              Dashboard
            </a>
            <a routerLink="/chat" routerLinkActive="is-active">
              Chat
            </a>
            <a routerLink="/profile" routerLinkActive="is-active">
              Profile
            </a>
          </nav>

          <main class="shell__content">
            <router-outlet />
          </main>
        </div>
      } @else {
        <router-outlet />
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background:
        radial-gradient(circle at top right, rgba(255, 224, 30, 0.08), transparent 28rem),
        linear-gradient(180deg, #111318 0%, #0b0c10 100%);
      color: #ffffff;
      font-family: Inter, Roboto, system-ui, sans-serif;
    }

    .shell {
      min-height: 100vh;
    }

    .shell__header {
      padding: 24px 32px;
      border-bottom: 1px solid #2a2a2a;
      background: rgba(11, 12, 16, 0.92);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
    }

    .shell__brand {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .shell__logo {
      width: 52px;
      height: 52px;
      object-fit: contain;
      filter: drop-shadow(0 10px 18px rgba(255, 224, 30, 0.18));
    }

    .shell__header h1 {
      margin: 4px 0 0;
      font-size: 2rem;
    }

    .shell__eyebrow {
      margin: 0;
      color: #ffe01e;
      text-transform: uppercase;
      letter-spacing: 0.12em;
      font-size: 0.75rem;
      font-weight: 700;
    }

    .shell__actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .shell__cta {
      min-height: 44px;
      padding: 10px 18px;
      border-radius: 12px;
      background: #ffe01e;
      color: #111318;
      text-decoration: none;
      font-weight: 700;
      display: inline-flex;
      align-items: center;
    }

    .shell__body {
      display: grid;
      grid-template-columns: 220px 1fr;
      min-height: calc(100vh - 105px);
    }

    .shell__nav {
      padding: 24px 16px;
      border-right: 1px solid #2a2a2a;
      background: rgba(18, 18, 18, 0.82);
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .shell__nav a {
      padding: 12px 14px;
      border-radius: 12px;
      color: #ffffff;
      text-decoration: none;
      font-weight: 600;
    }

    .shell__nav a.is-active,
    .shell__nav a:hover {
      background: rgba(255, 224, 30, 0.14);
      color: #ffe01e;
    }

    .shell__content {
      padding: 24px;
    }

    .shell__logout {
      min-height: 44px;
      padding: 0 18px;
      border-radius: 12px;
      border: 1px solid #2a2a2a;
      background: #121212;
      color: #ffffff;
      font: inherit;
      font-weight: 600;
      cursor: pointer;
    }

    @media (max-width: 900px) {
      .shell__body {
        grid-template-columns: 1fr;
      }

      .shell__nav {
        border-right: none;
        border-bottom: 1px solid #2a2a2a;
        flex-direction: row;
        overflow-x: auto;
      }

      .shell__header {
        align-items: start;
        flex-direction: column;
      }
    }
  `]
})
export class AppShellComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected showShell(): boolean {
    return this.router.url !== '/login';
  }

  protected currentSectionTitle(): string {
    if (this.router.url.startsWith('/chat')) {
      return 'AI Chat';
    }

    if (this.router.url.startsWith('/profile')) {
      return 'Profile';
    }

    return 'Dashboard';
  }

  protected logout(): void {
    this.authService.logout();
  }
}

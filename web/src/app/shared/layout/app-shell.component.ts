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
          <div>
            <p class="shell__eyebrow">Fitness AI Coach</p>
            <h1>Dashboard</h1>
          </div>

          <button type="button" class="shell__logout" (click)="logout()">Log out</button>
        </header>

        <div class="shell__body">
          <nav class="shell__nav" aria-label="Primary">
            <a routerLink="/dashboard" routerLinkActive="is-active" [routerLinkActiveOptions]="{ exact: true }">
              Dashboard
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
      align-items: end;
      justify-content: space-between;
      gap: 16px;
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
    }
  `]
})
export class AppShellComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected showShell(): boolean {
    return this.router.url !== '/login';
  }

  protected logout(): void {
    this.authService.logout();
  }
}

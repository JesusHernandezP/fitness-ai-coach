import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

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
        </header>

        <div class="shell__body">
          <nav class="shell__nav" aria-label="Primary">
            <a routerLink="/dashboard" routerLinkActive="is-active" [routerLinkActiveOptions]="{ exact: true }">
              Panel
            </a>
            <a routerLink="/chat" routerLinkActive="is-active">
              Chat
            </a>
            <a routerLink="/profile" routerLinkActive="is-active">
              Perfil
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
      width: 56px;
      height: 56px;
      object-fit: contain;
      filter: drop-shadow(0 10px 18px rgba(255, 224, 30, 0.18));
      flex-shrink: 0;
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
      padding: 8px 16px;
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
      padding: 32px;
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

  protected showShell(): boolean {
    return this.router.url !== '/login';
  }

  protected currentSectionTitle(): string {
    if (this.router.url.startsWith('/chat')) {
      return 'Chat con AI';
    }

    if (this.router.url.startsWith('/profile')) {
      return 'Perfil';
    }

    return 'Panel';
  }
}

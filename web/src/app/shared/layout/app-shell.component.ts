import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="shell">
      <header class="shell__header">
        <div>
          <p class="shell__eyebrow">Fitness AI Coach</p>
          <h1>Dashboard</h1>
        </div>
      </header>

      <div class="shell__body">
        <nav class="shell__nav" aria-label="Primary">
          <a routerLink="/dashboard" routerLinkActive="is-active" [routerLinkActiveOptions]="{ exact: true }">
            Dashboard
          </a>
          <a href="#" aria-disabled="true">Users</a>
          <a href="#" aria-disabled="true">Reports</a>
        </nav>

        <main class="shell__content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: linear-gradient(180deg, #f7fbfa 0%, #eef4f1 100%);
      color: #16302b;
      font-family: Arial, sans-serif;
    }

    .shell {
      min-height: 100vh;
    }

    .shell__header {
      padding: 24px 32px;
      border-bottom: 1px solid #d7e4df;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(8px);
    }

    .shell__header h1 {
      margin: 4px 0 0;
      font-size: 2rem;
    }

    .shell__eyebrow {
      margin: 0;
      color: #4d6c64;
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
      border-right: 1px solid #d7e4df;
      background: #f2f7f4;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .shell__nav a {
      padding: 12px 14px;
      border-radius: 12px;
      color: #285247;
      text-decoration: none;
      font-weight: 600;
    }

    .shell__nav a.is-active,
    .shell__nav a:hover {
      background: #dbece5;
    }

    .shell__content {
      padding: 24px;
    }

    @media (max-width: 900px) {
      .shell__body {
        grid-template-columns: 1fr;
      }

      .shell__nav {
        border-right: none;
        border-bottom: 1px solid #d7e4df;
        flex-direction: row;
        overflow-x: auto;
      }
    }
  `]
})
export class AppShellComponent {}

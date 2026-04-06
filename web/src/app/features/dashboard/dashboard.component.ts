import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <section class="dashboard">
      <article class="dashboard-card dashboard-card--wide">
        <h2>Weight Progress</h2>
        <p>Placeholder chart area for user weight evolution.</p>
      </article>

      <article class="dashboard-card">
        <h2>Weekly Summary</h2>
        <p>Placeholder summary for calories, adherence, and coaching context.</p>
      </article>

      <article class="dashboard-card">
        <h2>Activity Overview</h2>
        <p>Placeholder activity snapshot for steps and training consistency.</p>
      </article>
    </section>
  `,
  styles: [`
    .dashboard {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 20px;
    }

    .dashboard-card {
      background: #ffffff;
      border: 1px solid #d7e4df;
      border-radius: 20px;
      padding: 24px;
      box-shadow: 0 14px 34px rgba(22, 48, 43, 0.06);
      min-height: 180px;
    }

    .dashboard-card h2 {
      margin: 0 0 12px;
      font-size: 1.25rem;
    }

    .dashboard-card p {
      margin: 0;
      color: #49645d;
      line-height: 1.5;
    }

    .dashboard-card--wide {
      grid-column: 1 / -1;
      min-height: 260px;
    }

    @media (max-width: 900px) {
      .dashboard {
        grid-template-columns: 1fr;
      }

      .dashboard-card--wide {
        grid-column: auto;
      }
    }
  `]
})
export class DashboardComponent {}

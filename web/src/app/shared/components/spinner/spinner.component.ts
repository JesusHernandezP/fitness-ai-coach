import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="spinner" [class.spinner--small]="size === 'small'" [class.spinner--large]="size === 'large'">
      <div class="spinner__circle"></div>
    </div>
  `,
  styles: [`
    .spinner {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .spinner__circle {
      border: 3px solid rgba(255, 224, 30, 0.2);
      border-top: 3px solid #ffe01e;
      border-radius: 50%;
      width: 32px;
      height: 32px;
      animation: spin 0.8s linear infinite;
    }

    .spinner--small .spinner__circle {
      width: 20px;
      height: 20px;
      border-width: 2px;
    }

    .spinner--large .spinner__circle {
      width: 48px;
      height: 48px;
      border-width: 4px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class SpinnerComponent {
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
}

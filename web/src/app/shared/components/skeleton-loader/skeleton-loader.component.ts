import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-loader" [class.skeleton-loader--pulse]="pulse">
      @for (block of blocks; track block) {
        <div
          class="skeleton-loader__block"
          [style.width.%]="block.width"
          [style.height.px]="block.height"
          [style.margin-bottom.px]="block.marginBottom"
        ></div>
      }
    </div>
  `,
  styles: [`
    .skeleton-loader {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .skeleton-loader--pulse .skeleton-loader__block {
      animation: pulse 1.5s ease-in-out infinite;
    }

    .skeleton-loader__block {
      background: linear-gradient(90deg, #2a2a2a 25%, #3a3a3a 50%, #2a2a2a 75%);
      background-size: 200% 100%;
      border-radius: 8px;
    }

    @keyframes pulse {
      0%, 100% {
        opacity: 1;
      }
      50% {
        opacity: 0.4;
      }
    }
  `]
})
export class SkeletonLoaderComponent {
  @Input() blocks: Array<{ width: number; height: number; marginBottom?: number }> = [
    { width: 100, height: 20 },
    { width: 80, height: 16 },
    { width: 60, height: 16 }
  ];
  @Input() pulse: boolean = true;
}

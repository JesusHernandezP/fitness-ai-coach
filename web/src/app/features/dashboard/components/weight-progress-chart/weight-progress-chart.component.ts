import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, DestroyRef, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { Chart, LineController, LineElement, LinearScale, PointElement, CategoryScale, Tooltip, Legend, Filler } from 'chart.js';
import { ApiService } from '../../../../core/api/api.service';

Chart.register(LineController, LineElement, LinearScale, PointElement, CategoryScale, Tooltip, Legend, Filler);

@Component({
  selector: 'app-weight-progress-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './weight-progress-chart.component.html',
  styleUrl: './weight-progress-chart.component.css'
})
export class WeightProgressChartComponent implements AfterViewInit, OnDestroy {
  @ViewChild('chartCanvas')
  private chartCanvas?: ElementRef<HTMLCanvasElement>;

  private readonly apiService = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected isLoading = true;
  protected errorMessage = '';
  protected dataPoints: { date: string; weight: number }[] = [];
  protected startWeight: number | null = null;
  protected currentWeight: number | null = null;
  protected totalChange: number | null = null;

  private chart?: Chart;

  ngAfterViewInit(): void {
    const subscription = this.apiService.getWeightProgress().subscribe({
      next: (progress) => {
        this.dataPoints = progress;
        this.startWeight = progress.length > 0 ? progress[0].weight : null;
        this.currentWeight = progress.length > 0 ? progress[progress.length - 1].weight : null;
        this.totalChange =
          this.startWeight !== null && this.currentWeight !== null
            ? Number((this.currentWeight - this.startWeight).toFixed(1))
            : null;
        this.isLoading = false;

        if (progress.length === 0) {
          return;
        }

        this.renderChart(progress);
      },
      error: () => {
        this.errorMessage = 'Unable to load weight progress right now.';
        this.isLoading = false;
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private renderChart(progress: { date: string; weight: number }[]): void {
    const canvas = this.chartCanvas?.nativeElement;
    if (!canvas) {
      return;
    }

    this.chart?.destroy();

    this.chart = new Chart(canvas, {
      type: 'line',
      data: {
        labels: progress.map((point) => point.date),
        datasets: [
          {
            label: 'Weight (kg)',
            data: progress.map((point) => point.weight),
            borderColor: '#ffe01e',
            backgroundColor: 'rgba(255, 224, 30, 0.12)',
            fill: true,
            tension: 0.3,
            pointRadius: 4,
            pointHoverRadius: 5,
            pointBackgroundColor: '#ffe01e',
            pointBorderColor: '#111318'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          x: {
            grid: {
              color: 'rgba(51, 51, 51, 0.4)'
            },
            ticks: {
              color: '#a0a0a0'
            }
          },
          y: {
            beginAtZero: false,
            grid: {
              color: 'rgba(51, 51, 51, 0.4)'
            },
            ticks: {
              color: '#a0a0a0',
              callback: (value) => `${value} kg`
            }
          }
        }
      }
    });
  }

  protected get trendLabel(): string {
    if (this.totalChange === null) {
      return '--';
    }
    if (this.totalChange < 0) {
      return `${Math.abs(this.totalChange)} kg down`;
    }
    if (this.totalChange > 0) {
      return `${this.totalChange} kg up`;
    }
    return 'No change';
  }
}

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

  private chart?: Chart;

  ngAfterViewInit(): void {
    const subscription = this.apiService.getWeightProgress().subscribe({
      next: (progress) => {
        this.dataPoints = progress;
        this.isLoading = false;
        console.debug('weight-progress:data', progress);

        if (progress.length === 0) {
          return;
        }

        this.renderChart(progress);
      },
      error: (error) => {
        this.errorMessage = 'No se pudo cargar el progreso de peso en este momento.';
        this.isLoading = false;
        console.error('weight-progress:error', error);
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
            label: 'Peso (kg)',
            data: progress.map((point) => point.weight),
            borderColor: '#2c6a5b',
            backgroundColor: 'rgba(44, 106, 91, 0.14)',
            fill: true,
            tension: 0.3,
            pointRadius: 4,
            pointHoverRadius: 5
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
              display: false
            }
          },
          y: {
            beginAtZero: false,
            ticks: {
              callback: (value) => `${value} kg`
            }
          }
        }
      }
    });
  }
}

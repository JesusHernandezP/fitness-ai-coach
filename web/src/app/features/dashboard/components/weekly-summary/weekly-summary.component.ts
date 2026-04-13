import { CommonModule, formatDate } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ApiService, WeeklySummaryDto } from '../../../../core/api/api.service';

@Component({
  selector: 'app-weekly-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './weekly-summary.component.html',
  styleUrl: './weekly-summary.component.css'
})
export class WeeklySummaryComponent implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected isLoading = true;
  protected errorMessage = '';
  protected summaryData: WeeklySummaryDto | null = null;

  ngOnInit(): void {
    const subscription = this.apiService.getWeeklySummary().subscribe({
      next: (summary) => {
        this.summaryData = this.hasContent(summary) ? summary : null;
        this.isLoading = false;
        console.debug('weekly-summary:data', summary);
      },
      error: (error) => {
        this.errorMessage = 'No se pudo cargar el resumen semanal de AI en este momento.';
        this.isLoading = false;
        console.error('weekly-summary:error', error);
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  protected get summaryLabel(): string {
    if (!this.summaryData?.weekStart || !this.summaryData?.weekEnd) {
      return '';
    }

    const start = formatDate(this.summaryData.weekStart, 'dd MMM', 'es-ES');
    const end = formatDate(this.summaryData.weekEnd, 'dd MMM', 'es-ES');
    return start === end ? start : `${start} - ${end}`;
  }

  private hasContent(summary: WeeklySummaryDto): boolean {
    return Boolean(summary.summary?.trim() || summary.recommendation?.trim());
  }
}

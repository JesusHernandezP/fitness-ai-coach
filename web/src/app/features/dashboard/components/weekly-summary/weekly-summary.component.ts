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
      },
      error: () => {
        this.errorMessage = 'Unable to load the weekly AI summary right now.';
        this.isLoading = false;
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  protected get weekRangeLabel(): string {
    if (!this.summaryData?.weekStart || !this.summaryData?.weekEnd) {
      return '';
    }

    const start = formatDate(this.summaryData.weekStart, 'dd MMM', 'en-US');
    const end = formatDate(this.summaryData.weekEnd, 'dd MMM', 'en-US');
    return `${start} - ${end}`;
  }

  private hasContent(summary: WeeklySummaryDto): boolean {
    return Boolean(summary.summary?.trim() || summary.recommendation?.trim());
  }
}

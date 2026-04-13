import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ApiService } from '../../../../core/api/api.service';

type ActivityOverview = {
  date: string;
  totalSteps: number;
  totalMeals: number;
  totalWorkoutSessions: number;
  totalCaloriesConsumed: number;
  totalCaloriesBurned: number;
};

@Component({
  selector: 'app-activity-overview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activity-overview.component.html',
  styleUrl: './activity-overview.component.css'
})
export class ActivityOverviewComponent implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected isLoading = true;
  protected errorMessage = '';
  protected overview: ActivityOverview | null = null;

  ngOnInit(): void {
    const subscription = this.apiService.getTodayActivitySummary().subscribe({
      next: (summary) => {
        this.overview = summary;
        this.isLoading = false;
        console.debug('activity-overview:summary', summary);
      },
      error: (error) => {
        this.errorMessage = 'No se pudo cargar el resumen de actividad en este momento.';
        this.isLoading = false;
        console.error('activity-overview:error', error);
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }
}

import { Component } from '@angular/core';
import { WeightProgressChartComponent } from './components/weight-progress-chart/weight-progress-chart.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [WeightProgressChartComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {}

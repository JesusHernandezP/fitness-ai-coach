import { Component } from '@angular/core';
import { ChatComponent } from '../chat/chat.component';
import { ActivityOverviewComponent } from './components/activity-overview/activity-overview.component';
import { WeightProgressChartComponent } from './components/weight-progress-chart/weight-progress-chart.component';
import { WeeklySummaryComponent } from './components/weekly-summary/weekly-summary.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [WeightProgressChartComponent, WeeklySummaryComponent, ActivityOverviewComponent, ChatComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {}

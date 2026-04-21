import { Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { HealthComponent } from './health/health';
import { MarketsComponent } from './markets/markets';
import { AnalysisComponent } from './analysis/analysis';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: Dashboard },
  { path: 'register', loadComponent: () => import('./register/register').then(m => m.RegisterComponent) },
  { path: 'health', component: HealthComponent },
  { path: 'markets', component: MarketsComponent },
  { path: 'analysis', component: AnalysisComponent },
  { path: 'crypto', component: Dashboard }, 
  { path: '**', redirectTo: 'dashboard' },
];

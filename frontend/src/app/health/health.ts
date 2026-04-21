import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SignalService } from '../services/signal.service';

@Component({
  selector: 'app-health',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './health.html',
  styles: [`
    :host { display: block; min-height: 100vh; background: #000; }
    .glass-card {
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.08);
      backdrop-filter: blur(20px);
    }
    progress::-webkit-progress-value {
      background: linear-gradient(to right, #ef4444, #10b981);
      border-radius: 999px;
    }
    progress::-webkit-progress-bar {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 999px;
    }
  `]
})
export class HealthComponent {
  private signalService = inject(SignalService);
  
  searchSymbol = signal<string>('RELIANCE.NS');
  healthData = signal<any>(null);
  isLoading = signal<boolean>(false);

  fetchHealth() {
    if (!this.searchSymbol()) return;
    
    this.isLoading.set(true);
    // Ensure .NS or .BSE suffix if searching Indian stocks
    let sym = this.searchSymbol().toUpperCase();
    if (!sym.includes('.') && !sym.includes('-')) {
        sym += '.NS';
    }

    this.signalService.fetchHealthData(sym).subscribe({
      next: (data: any) => {
        this.healthData.set(data);
        this.isLoading.set(false);
      },
      error: (err: any) => {
        console.error('Health fetch failed', err);
        this.isLoading.set(false);
      }
    });
  }

  ngOnInit() {
    this.fetchHealth();
  }

  getScoreColor(score: number): string {
    if (score >= 80) return 'text-emerald-400';
    if (score >= 60) return 'text-cyan-400';
    if (score >= 40) return 'text-yellow-400';
    return 'text-red-400';
  }

  formatCurrency(val: number): string {
    if (!val) return 'N/A';
    if (val >= 1e12) return (val / 1e12).toFixed(2) + 'T';
    if (val >= 1e9) return (val / 1e9).toFixed(2) + 'B';
    if (val >= 1e7) return (val / 1e7).toFixed(2) + 'Cr';
    return val.toLocaleString();
  }
}

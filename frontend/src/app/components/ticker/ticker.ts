import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SignalService } from '../../services/signal.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-ticker',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-[#1e222d] border-b border-white/5 py-1.5 px-4 overflow-hidden whitespace-nowrap relative">
      <div class="flex items-center gap-12 animate-ticker hover:pause">
        @for (item of items(); track item.symbol) {
          <div class="flex items-center gap-2 group cursor-default">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-tighter">{{ item.symbol }}</span>
            <span class="text-[11px] font-black text-white tabular-nums">{{ item.price }}</span>
            <span class="text-[9px] font-black flex items-center {{ item.trend === 'up' ? 'text-emerald-400' : 'text-red-400' }}">
              {{ item.trend === 'up' ? '▲' : '▼' }} {{ item.change }}
            </span>
          </div>
        }
        <!-- Duplicate for Infinite Scroll -->
        @for (item of items(); track 'dup-'+item.symbol) {
          <div class="flex items-center gap-2 group opacity-50">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-tighter">{{ item.symbol }}</span>
            <span class="text-[11px] font-black text-white tabular-nums">{{ item.price }}</span>
            <span class="text-[9px] font-black flex items-center {{ item.trend === 'up' ? 'text-emerald-400' : 'text-red-400' }}">
              {{ item.trend === 'up' ? '▲' : '▼' }} {{ item.change }}
            </span>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    @keyframes ticker {
      0% { transform: translateX(0); }
      100% { transform: translateX(-50%); }
    }
    .animate-ticker {
      display: inline-flex;
      animation: ticker 60s linear infinite;
    }
    .hover\:pause:hover {
      animation-play-state: paused;
    }
  `]
})
export class TickerComponent implements OnInit, OnDestroy {
  private signalService = inject(SignalService);
  private refreshSub?: Subscription;

  items = signal<any[]>([]);
  symbols = ['^NSEI', '^NSEBANK', 'NIFTY_FIN_SERVICE.NS', '^NSEMDCP50', 'BTC-USD', 'ETH-USD', '^BSESN', 'RELIANCE.NS', 'SBIN.NS'];

  ngOnInit() {
    this.fetchData();
    this.refreshSub = interval(20000).subscribe(() => this.fetchData());
  }

  fetchData() {
    this.signalService.fetchMarketQuotes(this.symbols).subscribe({
      next: (val) => this.items.set(val),
      error: (err) => console.error('Ticker fetch error', err)
    });
  }

  ngOnDestroy() {
    this.refreshSub?.unsubscribe();
  }
}

import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SignalService } from '../services/signal.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-markets',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-[#0a0e14] text-white">
      <div class="max-w-7xl mx-auto px-6 py-12">
          
          <div class="flex items-center justify-between mb-12">
            <div>
              <h1 class="text-5xl font-black text-white tracking-tighter mb-2">Global <span class="text-emerald-400">Markets</span></h1>
              <p class="text-gray-500 uppercase tracking-widest text-[10px] font-bold">Real-Time Neural Market Flow & Crypto Pulse</p>
            </div>
            <div class="flex items-center gap-2 px-4 py-2 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl">
              <span class="relative flex h-2 w-2">
                <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75"></span>
                <span class="relative inline-flex h-2 w-2 rounded-full bg-emerald-500"></span>
              </span>
              <span class="text-[10px] text-emerald-400 font-bold uppercase tracking-widest">Live Connect Active</span>
            </div>
          </div>
          
          <!-- Market Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
              @for (item of marketSummary(); track item.symbol) {
                  <div class="bg-[#1e222d] border border-white/5 rounded-[2rem] p-8 hover:border-emerald-500/30 transition-all cursor-default group overflow-hidden relative">
                      <div class="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                        <span class="text-4xl font-black">#</span>
                      </div>
                      
                      <p class="text-[10px] text-gray-500 uppercase font-black mb-2 tracking-widest">{{ item.name }}</p>
                      <div class="flex flex-col">
                          <span class="text-3xl font-black text-white tabular-nums mb-2 tracking-tight">{{ item.price }}</span>
                          <div class="flex items-center gap-2">
                            <span class="px-2 py-0.5 rounded text-[10px] font-black {{ item.trend === 'up' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400' }}">
                                {{ item.change }}
                            </span>
                            <span class="text-[8px] text-gray-600 uppercase font-black">24h Change</span>
                          </div>
                      </div>
                  </div>
              }
          </div>

          <!-- Analysis Section -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div class="lg:col-span-2 bg-gradient-to-br from-[#1e222d] to-[#0a0e14] border border-white/5 rounded-[3rem] p-12 relative overflow-hidden">
                <div class="relative z-10">
                  <h2 class="text-3xl font-black text-white mb-6 uppercase tracking-tighter">Live Neural Network Scan</h2>
                  <p class="text-gray-400 text-sm leading-relaxed mb-8 max-w-xl">
                    Our AI models are currently scanning over 5,000+ global instruments. Real-time volatility and volume pulse are being analyzed every 5 seconds to detect the next big breakout.
                  </p>
                  <div class="flex flex-wrap gap-3">
                      @for (tag of ['NIFTY', 'S&P 500', 'NASDAQ', 'BITCOIN', 'GOLD']; track tag) {
                        <span class="px-4 py-2 bg-white/5 border border-white/10 rounded-xl text-[10px] font-black text-gray-500">{{ tag }}</span>
                      }
                  </div>
                </div>
                <!-- Background Decoration -->
                <div class="absolute -right-20 -bottom-20 w-80 h-80 bg-emerald-500/5 rounded-full blur-[100px]"></div>
            </div>

            <div class="bg-[#1e222d] border border-white/5 rounded-[3rem] p-10 flex flex-col justify-center items-center text-center">
                <div class="w-16 h-16 bg-emerald-500/10 rounded-2xl flex items-center justify-center text-emerald-400 text-2xl mb-6">⚡</div>
                <h3 class="text-xl font-black text-white mb-2 uppercase tracking-wide">High Speed</h3>
                <p class="text-xs text-gray-500">Direct exchange connectivity ensured through Yahoo Finance Pulse.</p>
            </div>
          </div>

      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
  `]
})
export class MarketsComponent implements OnInit, OnDestroy {
  private signalService = inject(SignalService);
  private refreshSub?: Subscription;

  marketSummary = signal<any[]>([]);

  symbols = ['^NSEI', '^NSEBANK', 'NIFTY_FIN_SERVICE.NS', '^NSEMDCP50', 'BTC-USD', 'ETH-USD', '^GSPC', '^IXIC', 'GC=F'];

  ngOnInit() {
    this.fetchData();
    // Refresh every 10 seconds for live feel
    this.refreshSub = interval(10000).subscribe(() => this.fetchData());
  }

  fetchData() {
    this.signalService.fetchMarketQuotes(this.symbols).subscribe({
      next: (val) => {
        this.marketSummary.set(val);
      },
      error: (err) => console.error('Market fetch failed', err)
    });
  }

  ngOnDestroy() {
    this.refreshSub?.unsubscribe();
  }
}

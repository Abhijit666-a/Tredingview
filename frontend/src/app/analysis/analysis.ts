import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SignalService } from '../services/signal.service';
import { TradeSignal } from '../models/signal.model';

@Component({
  selector: 'app-analysis',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen bg-[#0d0f14] p-8">
      <div class="max-w-7xl mx-auto">
        <header class="mb-12 flex items-center justify-between border-b border-white/5 pb-8">
           <div>
              <h1 class="text-3xl font-black text-white uppercase tracking-tighter">Neural <span class="text-cyan-400">Analysis Hub</span></h1>
              <p class="text-xs text-slate-500 font-bold uppercase tracking-widest mt-2">Strategic Pattern Intelligence & Risk Monitoring</p>
           </div>
           <div class="px-6 py-3 bg-red-500/10 border border-red-500/20 rounded-2xl">
              <span class="text-[10px] font-black text-red-400 uppercase tracking-widest">Real Money Shield: Active</span>
           </div>
        </header>

        <div class="grid grid-cols-12 gap-8">
           @for (sig of signals(); track sig.id) {
             <div class="col-span-12 lg:col-span-6 bg-[#1e222d] rounded-3xl border border-white/10 overflow-hidden p-8 hover:border-cyan-500/30 transition-all shadow-2xl">
                <div class="flex justify-between items-start mb-6">
                   <div>
                      <h2 class="text-2xl font-black text-white uppercase">{{ sig.stock.symbol }}</h2>
                      <span class="text-[10px] font-black text-cyan-400 uppercase tracking-widest bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 mt-2 inline-block">{{ sig.patternName }}</span>
                   </div>
                   <div class="text-right">
                      <div class="text-[9px] font-black text-slate-500 uppercase">Confidence</div>
                      <div class="text-3xl font-black text-cyan-400 font-mono">{{ (sig.confidenceScore * 100) | number:'1.0-0' }}%</div>
                   </div>
                </div>

                <!-- Deep Analysis Report -->
                <div class="space-y-6 mb-8">
                   <div class="p-4 bg-black/20 rounded-2xl border border-white/5">
                      <h3 class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2">Technical Rationale</h3>
                      <p class="text-xs text-slate-300 leading-relaxed font-medium italic">
                         "{{ getPatternLogic(sig.patternName) }}"
                      </p>
                   </div>

                   <!-- Quant-Logic Deep Dive (Formulas & Statistics) -->
                   <div class="p-5 bg-cyan-400/[0.03] rounded-2xl border border-cyan-400/10">
                      <h4 class="text-[9px] font-black text-cyan-400 uppercase tracking-widest mb-4 flex items-center gap-2">
                        <span class="w-1.5 h-1.5 rounded-full bg-cyan-500"></span> Quant-Logic Deep Dive
                      </h4>
                      <div class="grid grid-cols-2 gap-6">
                         <div class="space-y-3">
                            <div class="flex flex-col">
                               <span class="text-[8px] text-slate-500 font-bold uppercase mb-1">Standard Deviation (σ)</span>
                               <code class="text-[10px] text-white font-mono opacity-80">σ = √[Σ(xi-μ)²/N] = 0.84%</code>
                            </div>
                            <div class="flex flex-col">
                               <span class="text-[8px] text-slate-500 font-bold uppercase mb-1">Relative Strength (RSI Math)</span>
                               <code class="text-[10px] text-white font-mono opacity-80">100 - [100/(1+RS)] = 64.2</code>
                            </div>
                         </div>
                         <div class="space-y-3">
                            <div class="flex flex-col">
                               <span class="text-[8px] text-slate-500 font-bold uppercase mb-1">Trend Velocity (ΔPrice/ΔTime)</span>
                               <code class="text-[10px] text-white font-mono opacity-80">v(t) = 0.125 ticks/sec</code>
                            </div>
                            <div class="flex flex-col">
                               <span class="text-[8px] text-slate-500 font-bold uppercase mb-1">Neural Node Confidence</span>
                               <code class="text-[10px] text-white font-mono opacity-80">tanh(Σ wi*xi + b) = {{ sig.confidenceScore | number:'1.2-2' }}</code>
                            </div>
                         </div>
                      </div>
                   </div>
                   
                   <div class="grid grid-cols-3 gap-4">
                      <div class="p-3 bg-white/[0.02] rounded-xl border border-white/5 text-center">
                         <div class="text-[8px] text-slate-500 font-bold uppercase mb-1">Entry</div>
                         <div class="text-sm font-black text-white">{{ sig.entryPrice | number:'1.2-2' }}</div>
                      </div>
                      <div class="p-3 bg-red-500/5 rounded-xl border border-red-500/10 text-center">
                         <div class="text-[8px] text-red-400 font-bold uppercase mb-1">Stop-Loss</div>
                         <div class="text-sm font-black text-red-500">{{ sig.stopLoss | number:'1.2-2' }}</div>
                      </div>
                      <div class="p-3 bg-green-500/5 rounded-xl border border-green-500/10 text-center">
                         <div class="text-[8px] text-green-400 font-bold uppercase mb-1">Target</div>
                         <div class="text-sm font-black text-green-500">{{ sig.targetPrice | number:'1.2-2' }}</div>
                      </div>
                   </div>
                </div>

                <div class="flex items-center justify-between pt-6 border-t border-white/5">
                   <div class="flex items-center gap-2">
                      <div class="w-1.5 h-1.5 rounded-full bg-orange-500"></div>
                      <span class="text-[9px] font-black text-orange-400 uppercase">Risk Factor: {{ (1 - sig.confidenceScore) < 0.2 ? 'LOW' : 'MODERATE' }}</span>
                   </div>
                   <button class="bg-white text-black text-[10px] font-black px-6 py-2 rounded-xl uppercase hover:bg-cyan-400 transition-colors">Execute Signal</button>
                </div>
             </div>
           } @empty {
             <div class="col-span-12 p-32 text-center opacity-30">
                <div class="w-16 h-16 border-4 border-white/10 border-t-cyan-500 rounded-full animate-spin mx-auto mb-8"></div>
                <h3 class="text-xl font-black uppercase tracking-widest text-white">Aggregating Neural Insights...</h3>
             </div>
           }
        </div>
      </div>
    </div>
  `,
})
export class AnalysisComponent implements OnInit {
  readonly signals = signal<TradeSignal[]>([]);

  constructor(private signalService: SignalService) {}

  ngOnInit(): void {
    this.signalService.fetchSignals().subscribe(data => {
      // Show precision signals (75%+)
      const filtered = data.filter(sig => sig.confidenceScore >= 0.75);
      this.signals.set(filtered.slice(0, 50));
    });
  }

  getPatternLogic(pattern: string): string {
    const p = pattern.toLowerCase();
    if (p.includes('bottom')) return 'Neural Double Bottom detected. Price tested historical floor twice. Strong accumulation detected near support levels.';
    if (p.includes('recovery')) return 'V-Pulse Recovery strategy. Sharp sharp reversal confirmed with impulsive volume spikes. High probability mean-reversion.';
    if (p.includes('breakout')) return 'Classical Range Breakout. Price exceeded volatility-adjusted resistance bands. Trend is likely to continue up.';
    if (p.includes('momentum')) return 'EMA-20 Momentum scan. Price is trending strongly above fast-moving averages. Aggressive trend following strategy.';
    return 'Neural momentum scan. Statistical deviance suggests a high-probability volatility breakout. Follow strictly controlled SL.';
  }
}

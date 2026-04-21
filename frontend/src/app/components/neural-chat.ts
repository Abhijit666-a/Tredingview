import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SignalService } from '../services/signal.service';
import { TradeSignal } from '../models/signal.model';

interface ChatMessage {
  id: number;
  sender: string;
  text: string;
  time: Date;
  type: 'system' | 'signal' | 'alert';
}

@Component({
  selector: 'app-neural-chat',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-[#1e222d] rounded-3xl border border-white/10 overflow-hidden shadow-2xl h-full flex flex-col">
      <div class="px-6 py-4 border-b border-white/5 flex items-center justify-between bg-white/[0.02]">
        <div class="flex items-center gap-2">
          <div class="w-2 h-2 rounded-full bg-cyan-500 animate-pulse"></div>
          <h2 class="text-[10px] font-black uppercase tracking-[0.2em] text-cyan-400">Neural Chat Bot</h2>
        </div>
        <span class="text-[8px] font-bold text-slate-500 uppercase tracking-widest">Auto-Streaming</span>
      </div>
      
      <div class="flex-grow p-4 overflow-y-auto space-y-4 scrollbar-hide" #scrollContainer>
        @for (msg of messages(); track msg.id) {
          <div class="flex flex-col gap-1" [class.items-end]="msg.type === 'alert'">
            <div class="flex items-center gap-2">
              <span class="text-[9px] font-black uppercase tracking-widest text-cyan-500">{{ msg.sender }}</span>
              <span class="text-[8px] text-slate-600 font-bold">{{ msg.time | date:'HH:mm:ss' }}</span>
            </div>
            <div [class]="getMessageClass(msg.type)" class="px-4 py-2 rounded-2xl text-[11px] font-medium max-w-[90%] border">
              {{ msg.text }}
            </div>
          </div>
        }
      </div>

      <div class="p-4 border-t border-white/5 bg-black/20">
        <div class="relative">
          <input type="text" placeholder="NEURAL COMMAND..." readonly
                 class="w-full bg-[#161922] border border-white/5 rounded-xl px-4 py-2 text-[10px] text-slate-500 focus:outline-none italic">
          <div class="absolute right-3 top-1/2 -translate-y-1/2 text-[10px] font-black text-cyan-500 animate-pulse">AI PROCESSING</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .scrollbar-hide::-webkit-scrollbar { display: none; }
    .scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
  `]
})
export class NeuralChatComponent implements OnInit {
  private readonly signalService = inject(SignalService);
  readonly messages = signal<ChatMessage[]>([]);
  private msgId = 0;
  private lastPostedSignals = new Map<string, number>();

  ngOnInit() {
    this.addMessage('System', 'Neural Logic initialized. Scanning global markets...', 'system');
    
    // Subscribe to real-time signals to auto-post to chat
    this.signalService.streamSignalsRealtime().subscribe(sig => {
      const now = Date.now();
      const lastTime = this.lastPostedSignals.get(sig.stock.symbol) || 0;
      
      // Only post if it's a new signal or at least 2 minutes have passed for the same symbol
      if (now - lastTime > 120000) {
        const type = sig.signalType === 'CALL' ? 'BUY (CALL)' : 'SELL (PUT)';
        this.addMessage('AI-SIGNAL', `🔥 NEW BREAKOUT: ${sig.stock.symbol} ${type} detected at ₹${sig.entryPrice.toFixed(2)}. Target: ₹${sig.targetPrice.toFixed(2)}. Confidence: ${(sig.confidenceScore * 100).toFixed(0)}%`, 'signal');
        this.lastPostedSignals.set(sig.stock.symbol, now);
      }
    });

    // Random AI insights & Exit Strategy
    setInterval(() => {
      const chance = Math.random();
      if (chance > 0.8) {
        const insights = [
            'PROFIT BOOKING ALERT: Nifty approaching major resistance. Consider partial sell.',
            'EXIT STRATEGY: Maintain strict Stop Loss on TCS PE as volume is decreasing.',
            'NEURAL ADVICE: For EICHERMOT CALL, trailing stop loss to entry price recommended.',
            'SELL SIGNAL UPDATE: Reliance target R1 almost reached. Prepare to exit.',
            'MARKET PSYCHOLOGY: Greed index is high. Good time to book profits in intraday longs.'
        ];
        this.addMessage('AI-BOT', insights[Math.floor(Math.random() * insights.length)], 'alert');
      }
    }, 20000);
  }

  addMessage(sender: string, text: string, type: 'system' | 'signal' | 'alert') {
    const newMsg: ChatMessage = { id: ++this.msgId, sender, text, time: new Date(), type };
    this.messages.update(prev => [...prev.slice(-49), newMsg]);
    
    // Auto scroll logic (simplified)
    setTimeout(() => {
      const container = document.querySelector('.scrollbar-hide');
      if (container) container.scrollTop = container.scrollHeight;
    }, 100);
  }

  getMessageClass(type: string) {
    switch (type) {
      case 'signal': return 'bg-cyan-500/10 border-cyan-500/20 text-cyan-100';
      case 'alert': return 'bg-red-500/10 border-red-500/20 text-red-100';
      default: return 'bg-white/5 border-white/5 text-slate-300';
    }
  }
}

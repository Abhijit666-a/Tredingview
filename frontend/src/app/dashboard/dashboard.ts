import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription, interval } from 'rxjs';
import { SignalService } from '../services/signal.service';
import { TradeSignal } from '../models/signal.model';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NeuralChatComponent } from '../components/neural-chat';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit, OnDestroy {
  private subscription?: Subscription;
  private countdownSub?: Subscription;

  readonly signals = signal<TradeSignal[]>([]);
  private readonly takenSignalIds = signal<Set<number>>(new Set());
  readonly isLive = signal(false);
  readonly lastUpdated = signal<Date | null>(null);
  readonly countdown = signal<number>(5);
  readonly takenTrades = signal<TradeSignal[]>([]);
  private readonly currentPrices = signal<Map<string, number>>(new Map());

  // Modal State
  readonly isModalOpen = signal(false);
  readonly selectedSignal = signal<TradeSignal | null>(null);
  readonly selectedNews = signal<any[]>([]);
  readonly chartUrl = signal<SafeResourceUrl | null>(null);
  readonly monthlyReport = signal<any[]>([]);
  readonly headerStats = signal<any[]>([]);
  readonly optionChain = signal<any[]>([]);
  readonly activeTab = signal<'dashboard' | 'markets' | 'news'>('dashboard');

  // Capital & Filter State
  readonly userCapital = signal<number>(50000); 
  readonly viewMode = signal<'intraday' | 'monthly'>('intraday');

  updateCapital(event: any) {
    const val = parseFloat(event.target.value);
    if (!isNaN(val)) this.userCapital.set(val);
  }

  switchMode(event: any) {
    this.viewMode.set(event.target.value);
  }

  onScanSubmit(symbol: string) {
    if (!symbol) return;
    this.signalService.submitSymbol(symbol).subscribe({
      next: () => {
        console.log('✅ Symbol submitted for scanning:', symbol);
        this.isLive.set(true);
        this.fetchHeaderStats(); // Refresh header to show the new symbol if possible
        this.openChart(symbol);
      },
      error: (err) => console.error('❌ Failed to submit symbol:', err)
    });
  }

  onSearch(event: any) {
    const symbol = event.target.value.toUpperCase();
    if (symbol) {
      this.onScanSubmit(symbol);
    }
  }

  readonly uniqueSignals = computed(() => {
    const allSignals = this.signals();
    const latestMap = new Map<string, TradeSignal>();

    allSignals.forEach(sig => {
      const existing = latestMap.get(sig.stock.symbol);
      if (!existing || new Date(sig.generatedAt) > new Date(existing.generatedAt)) {
        latestMap.set(sig.stock.symbol, sig);
      }
    });

    return Array.from(latestMap.values())
      .filter(sig => sig.confidenceScore >= 0.75) 
      .sort((a, b) => {
        const aTaken = this.takenSignalIds().has(a.id);
        const bTaken = this.takenSignalIds().has(b.id);
        if (aTaken !== bTaken) return bTaken ? 1 : -1;
        
        return b.confidenceScore - a.confidenceScore || 
               new Date(b.generatedAt).getTime() - new Date(a.generatedAt).getTime();
      });
  });

  toggleTradeTaken(sig: TradeSignal) {
    const currentIds = new Set(this.takenSignalIds());
    let currentTrades = [...this.takenTrades()];

    if (currentIds.has(sig.id)) {
      currentIds.delete(sig.id);
      currentTrades = currentTrades.filter(t => t.id !== sig.id);
    } else {
      currentIds.add(sig.id);
      currentTrades.push(sig);
    }

    this.takenSignalIds.set(currentIds);
    this.takenTrades.set(currentTrades);
    
    // Persist to LocalStorage
    localStorage.setItem('tradeintel_taken_ids', JSON.stringify(Array.from(currentIds)));
    localStorage.setItem('tradeintel_taken_trades', JSON.stringify(currentTrades));
  }

  isTradeTaken(sigId: number): boolean {
    return this.takenSignalIds().has(sigId);
  }

  readonly totalSignals = computed(() => this.uniqueSignals().length);
  
  // Personal Performance Stats
  readonly takenStats = computed(() => {
    const list = this.takenTrades();
    const hits = list.filter(s => s.confidenceScore >= 0.9).length;
    const losses = list.filter(s => s.confidenceScore < 0.8 && s.confidenceScore > 0).length;
    const rate = list.length > 0 ? (hits / list.length) * 100 : 0;
    return { total: list.length, hits, losses, rate };
  });

  readonly callCount = computed(
    () => this.uniqueSignals().filter((s) => s.signalType === 'CALL').length,
  );
  readonly putCount = computed(
    () => this.uniqueSignals().filter((s) => s.signalType === 'PUT').length,
  );
  readonly avgConfidence = computed(() => {
    const sigs = this.uniqueSignals();
    if (sigs.length === 0) return 0;
    return sigs.reduce((sum, s) => sum + s.confidenceScore, 0) / sigs.length;
  });

  readonly optionSignals = computed(() => 
    this.uniqueSignals().filter(s => 
      s.stock.symbol.includes('CE') || 
      s.stock.symbol.includes('PE') || 
      s.stock.symbol.includes('CALL') || 
      s.stock.symbol.includes('PUT') ||
      s.stock.sector === 'Crypto' ||
      s.stock.sector === 'Index'
    )
  );

  readonly intradaySignals = computed(() => 
    this.uniqueSignals().filter(s => !this.optionSignals().includes(s))
  );

  constructor(
    private signalService: SignalService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    // 0. Load Persisted Trades
    const savedIds = localStorage.getItem('tradeintel_taken_ids');
    const savedTrades = localStorage.getItem('tradeintel_taken_trades');
    if (savedIds && savedTrades) {
      try {
        this.takenSignalIds.set(new Set(JSON.parse(savedIds)));
        this.takenTrades.set(JSON.parse(savedTrades));
      } catch (e) {
        console.error('Error loading persisted trades', e);
      }
    }

    // 1. Initial Load: Get recent historical signals
    this.signalService.fetchSignals().subscribe(data => {
      this.signals.set(data);
      if (data.length > 0) {
        this.isLive.set(true);
        // Show the chart for the most recent signal if available, otherwise NIFTY
        const firstSig = data[0];
        if (firstSig) {
          this.openChart(firstSig.stock.symbol);
        } else {
          this.openChart('NIFTY');
        }
      } else {
        this.openChart('NIFTY'); // Default to Nifty
      }
    });

    // 2. MILLISECOND STREAM: Direct from Webhook
    this.subscription = this.signalService.streamSignalsRealtime().subscribe({
      next: (newSignal) => {
        console.log('🚀 MILLISECOND SIGNAL RECEIVED:', newSignal);
        this.signals.update(current => [newSignal, ...current].slice(0, 50));
        this.isLive.set(true);
        this.lastUpdated.set(new Date());
      },
      error: (err) => {
        console.error('❌ Stream connection error:', err);
        this.isLive.set(false);
      },
    });

    // 3. LIVE PRICE STREAM: Listen for millisecond price updates
    const priceSub = this.signalService.streamPriceTicks().subscribe({
      next: (tick) => {
        this.headerStats.update(stats => {
          return stats.map(s => {
            const matches = s.symbol === tick.symbol || 
                          (s.symbol === '^NSEI' && tick.symbol === 'NIFTY') ||
                          (s.symbol === '^NSEBANK' && tick.symbol === 'BANKNIFTY') ||
                          (s.symbol === 'NIFTY_FIN_SERVICE.NS' && tick.symbol === 'FINNIFTY') ||
                          (s.symbol === '^NSEMDCP50' && tick.symbol === 'MIDCPNIFTY');
            
            if (matches) {
              return { ...s, price: tick.price };
            }
            return s;
          });
        });

        // Also update currentPrices map
        this.currentPrices.update(map => {
          const newMap = new Map(map);
          newMap.set(tick.symbol, tick.price);
          // Handle index mappings
          if (tick.symbol === 'NIFTY') newMap.set('^NSEI', tick.price);
          if (tick.symbol === 'BANKNIFTY') newMap.set('^NSEBANK', tick.price);
          return newMap;
        });

        this.isLive.set(true);
      }
    });
    this.subscription?.add(priceSub);

    // Tick every second to update countdown
    this.countdownSub = interval(1000).subscribe(() => {
      this.countdown.update((v) => (v > 0 ? v - 1 : 0));
    });

    // Fetch Monthly Recommendations
    this.signalService.fetchMonthlyReport().subscribe((data: any[]) => {
      this.monthlyReport.set(data);
    });

    // Fetch Header Stats Live
    this.fetchHeaderStats();
    interval(15000).subscribe(() => this.fetchHeaderStats());

    // Live Option Chain Polling
    this.refreshOptionChain();
    interval(10000).subscribe(() => this.refreshOptionChain());
  }

  refreshOptionChain() {
    this.signalService.fetchOptionChain('^NSEI').subscribe(data => {
      this.optionChain.set(data);
    });
  }

  fetchHeaderStats() {
    this.signalService.fetchMarketQuotes(['^NSEI', '^NSEBANK', 'NIFTY_FIN_SERVICE.NS', '^NSEMDCP50', '^BSESN', 'BTC-USD', 'ETH-USD', '^IXIC']).subscribe((data: any[]) => {
      this.headerStats.set(data);
      if (data.length > 0) this.isLive.set(true);
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.countdownSub?.unsubscribe();
  }

  openModal(sig: TradeSignal) {
    this.selectedSignal.set(sig);
    this.openChart(sig.stock.symbol, sig.stock.name);
    
    // Fetch News for the stock
    this.signalService.fetchNews(sig.stock.symbol).subscribe((news: any[]) => {
      this.selectedNews.set(news);
    });
  }

  openChart(symbol: string, name: string = '') {
    this.isModalOpen.set(true);
    let chartSymbol = symbol;
    
    // Normalize Premium Names back to standard symbols for TradingView
    if (chartSymbol.includes('CALL') || chartSymbol.includes('PUT')) {
      if (chartSymbol.includes('BTC') || chartSymbol.includes('ETH')) {
        const coin = chartSymbol.split(' ')[0];
        chartSymbol = `BINANCE:${coin}USDT`;
      } else if (chartSymbol.includes('NIFTY') || chartSymbol.includes('BANKNIFTY')) {
        chartSymbol = chartSymbol.includes('BANKNIFTY') ? 'NSE:BANKNIFTY' : 'NSE:NIFTY';
      }
    } else if (chartSymbol.endsWith('.NS')) {
      chartSymbol = `NSE:${chartSymbol.split('.')[0]}`;
    } else if (chartSymbol.endsWith('.BSE')) {
      chartSymbol = `BSE:${chartSymbol.split('.')[0]}`;
    } else if (chartSymbol.includes('-USD')) {
      const coin = chartSymbol.split('-')[0];
      chartSymbol = `BINANCE:${coin}USDT`;
    }
    
    const url = `https://s.tradingview.com/widgetembed/?frameElementId=tradingview_1&symbol=${chartSymbol}&interval=15&theme=dark&style=1&timezone=Asia%2FKolkata&locale=in`;
    this.chartUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
  }

  setTab(tab: 'dashboard' | 'markets' | 'news') {
    this.activeTab.set(tab);
  }

  closeModal() {
    this.isModalOpen.set(false);
    this.selectedSignal.set(null);
    this.selectedNews.set([]);
    this.chartUrl.set(null);
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('en-IN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }

  formatCountdown(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    if (h > 0) {
      return `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  getFlag(name: string): string {
    const n = name.toLowerCase();
    if (n.includes('nifty') || n.includes('bse') || n.includes('sensex') || n.includes('vix')) return '🇮🇳';
    if (n.includes('dow') || n.includes('s&p') || n.includes('nasdaq') || n.includes('jones') || n.includes('dji') || n.includes('gspc')) return '🇺🇸';
    if (n.includes('btc') || n.includes('eth')) return '🪙';
    if (n.includes('tsx')) return '🇨🇦';
    return '🏳️';
  }

  formatPrice(price: number | null | undefined): string {
    if (price === null || price === undefined) return '0.00';
    return price.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  calculatePricePosition(sig: TradeSignal): number {
    const symbol = sig.stock.symbol;
    // Try to find the latest price
    let currentPrice = this.currentPrices().get(symbol);
    
    // Fallback if symbol is in different format
    if (!currentPrice) {
      if (symbol.includes('BTC')) currentPrice = this.currentPrices().get('BTC-USD');
      if (symbol.includes('ETH')) currentPrice = this.currentPrices().get('ETH-USD');
    }

    if (!currentPrice) return 50; // Middle if no price yet

    const range = sig.targetPrice - sig.stopLoss;
    if (range <= 0) return 50;

    let pos = ((currentPrice - sig.stopLoss) / range) * 100;
    return Math.min(Math.max(pos, 2), 98); // Clamp with small buffer for visibility
  }

  getSLZoneWidth(sig: TradeSignal): number {
    const range = sig.targetPrice - sig.stopLoss;
    if (range <= 0) return 30;
    return ((sig.entryPrice - sig.stopLoss) / range) * 100;
  }

  getProfitZoneWidth(sig: TradeSignal): number {
    const range = sig.targetPrice - sig.stopLoss;
    if (range <= 0) return 70;
    return ((sig.targetPrice - sig.entryPrice) / range) * 100;
  }
}

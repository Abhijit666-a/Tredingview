/**
 * TradeIntel AI Dashboard Logic
 */

class TradeIntelApp {
    constructor() {
        this.prevPrice = 2940.00;
        this.apiBase = '/api/v1';
        this.signals = [];
        this.init();
    }

    init() {
        console.log("🚀 TradeIntel AI Dashboard Initialized");
        this.pollData();
        // Poll every 5 seconds for UI smoothness
        setInterval(() => this.pollData(), 5000);
    }

    async pollData() {
        try {
            // Update Health Status
            const health = await fetch(`${this.apiBase}/health`).then(r => r.json());
            document.getElementById('last-sync').textContent = new Date().toLocaleTimeString();
            
            // Get Signals
            const signalData = await fetch(`${this.apiBase}/signals`).then(r => r.json());
            this.updateSignals(signalData);

            // Get News
            const newsData = await fetch(`${this.apiBase}/news`).then(r => r.json());
            this.updateNewsSnapshot(newsData);

        } catch (error) {
            console.error("❌ Sync Error:", error);
            document.getElementById('engine-status').textContent = 'OFFLINE';
            document.getElementById('engine-status').className = 'status-val status-inactive';
        }
    }

    updateSignals(newSignals) {
        if (!newSignals || newSignals.length === 0) return;

        const container = document.getElementById('signals-list');
        const countSpan = document.getElementById('signal-count');
        
        // Update count
        countSpan.textContent = `${newSignals.length} Signals`;

        // If data has changed, re-render
        if (JSON.stringify(newSignals) !== JSON.stringify(this.signals)) {
            this.signals = newSignals;
            
            // Simulated Price update based on the latest signal or metadata
            if (this.signals.length > 0) {
                this.updateLivePrice(this.signals[0].entryPrice);
            }

            container.innerHTML = this.signals.map(s => `
                <div class="signal-card">
                    <div class="signal-type">
                        <span class="type-badge ${s.signalType === 'CALL' ? 'type-call' : 'type-put'}">${s.signalType}</span>
                    </div>
                    <div class="signal-pattern">
                        <span class="pattern-name">${s.patternName}</span>
                        <span class="signal-time">${this.formatTime(s.generatedAt)}</span>
                    </div>
                    <div class="signal-params">
                        <div class="param-item">
                            <span class="param-label">Entry</span>
                            <span class="param-value">₹${s.entryPrice.toFixed(2)}</span>
                        </div>
                        <div class="param-item">
                            <span class="param-label">Target</span>
                            <span class="param-value" style="color:var(--success)">₹${s.targetPrice.toFixed(2)}</span>
                        </div>
                    </div>
                    <div class="signal-confidence">
                        <span class="conf-label">Confidence</span>
                        <span class="conf-value">${(s.confidenceScore * 100).toFixed(1)}%</span>
                    </div>
                </div>
            `).join('');
        }
    }

    updateNewsSnapshot(news) {
        if (!news || news.length === 0) return;
        
        const latestEn = news[0];
        document.getElementById('latest-headline').textContent = latestEn.headline;
        document.getElementById('sentiment-summary').textContent = latestEn.aiSummary;
        
        // Update Indicator
        // sentimentScore is [-1, 1], map to [0, 100]
        const percentage = ((latestEn.sentimentScore + 1) / 2) * 100;
        const indicator = document.getElementById('sentiment-indicator');
        indicator.style.width = `${percentage}%`;
        
        // Change color based on sentiment
        if (latestEn.sentimentScore > 0.3) {
            indicator.style.boxShadow = '0 0 15px rgba(0, 255, 136, 0.5)';
        } else if (latestEn.sentimentScore < -0.3) {
            indicator.style.boxShadow = '0 0 15px rgba(255, 62, 62, 0.5)';
        } else {
            indicator.style.boxShadow = '0 0 15px rgba(255, 204, 0, 0.5)';
        }
    }

    updateLivePrice(price) {
        const priceEl = document.getElementById('current-price');
        const changeEl = document.getElementById('price-change');
        
        priceEl.textContent = price.toLocaleString('en-IN', { minimumFractionDigits: 2 });
        
        const diff = price - 2950.00;
        const changePercent = (diff / 2950.00) * 100;
        
        changeEl.textContent = `${changePercent >= 0 ? '+' : ''}${changePercent.toFixed(2)}%`;
        changeEl.className = `change-tag ${changePercent >= 0 ? 'positive' : 'negative'}`;
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    }
}

// Start App
document.addEventListener('DOMContentLoaded', () => {
    window.app = new TradeIntelApp();
});

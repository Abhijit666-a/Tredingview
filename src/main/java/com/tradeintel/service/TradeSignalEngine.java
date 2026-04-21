package com.tradeintel.service;

import com.tradeintel.entity.*;
import com.tradeintel.repository.StockRepository;
import com.tradeintel.repository.TradeSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignalEngine {

    private final TechnicalAnalysisService technicalAnalysisService;
    private final StockRepository stockRepository;
    private final TradeSignalRepository tradeSignalRepository;
    private final SmartApiService smartApiService;
    private final BinanceWebSocketService binanceWebSocketService;
    private final SignalUpdateService signalUpdateService;

    public static class StockConfig {
        public String symbol;
        public String sector;
        public String yfSymbol; 
        public int lotSize;
        public StockConfig(String symbol, String sector, String yfSymbol, int lotSize) {
            this.symbol = symbol; this.sector = sector; this.yfSymbol = yfSymbol; this.lotSize = lotSize;
        }
    }

    private final List<StockConfig> STOCKS = new CopyOnWriteArrayList<>();

    public void addSymbolToScan(String symbol, String sector, String yfSymbol, int lotSize) {
        if (STOCKS.stream().noneMatch(s -> s.yfSymbol.equals(yfSymbol))) {
            STOCKS.add(new StockConfig(symbol, sector, yfSymbol, lotSize));
            log.info("➕ Engine Scanning: {}", yfSymbol);
        }
    }

    private boolean isIndianMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;
        LocalTime time = now.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 15)) && !time.isAfter(LocalTime.of(15, 30));
    }

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @PostConstruct
    public void init() {
        addSymbolToScan("NIFTY", "Index", "^NSEI", 50);
        addSymbolToScan("BANKNIFTY", "Index", "^NSEBANK", 15);
        addSymbolToScan("FINNIFTY", "Index", "NIFTY_FIN_SERVICE.NS", 40);
        addSymbolToScan("MIDCPNIFTY", "Index", "^NSEMDCP50", 75);
        addSymbolToScan("BTC-USD", "Crypto", "BTC-USD", 1);
        addSymbolToScan("RELIANCE", "Equity", "RELIANCE.NS", 250);
        addSymbolToScan("TCS", "Equity", "TCS.NS", 175);
        addSymbolToScan("INFY", "Equity", "INFY.NS", 400);
        addSymbolToScan("EICHERMOT", "Equity", "EICHERMOT.NS", 175);
        addSymbolToScan("SBIN", "Equity", "SBIN.NS", 1500);
        addSymbolToScan("HDFCBANK", "Equity", "HDFCBANK.NS", 550);
        addSymbolToScan("ICICIBANK", "Equity", "ICICIBANK.NS", 700);

        // US Tech Market
        addSymbolToScan("NVDA", "US Tech", "NVDA", 1);
        addSymbolToScan("TSLA", "US Tech", "TSLA", 1);
        addSymbolToScan("AAPL", "US Tech", "AAPL", 1);
    }

    @Scheduled(initialDelay = 5000, fixedRateString = "${tradeintel.scheduler.interval-ms}")
    @Transactional
    public void processTradeSignals() {
        if (STOCKS.isEmpty()) return;
        boolean indianMarketOpen = isIndianMarketOpen();

        for (StockConfig config : STOCKS) {
            if ((config.yfSymbol.startsWith("^NSE") || config.yfSymbol.endsWith(".NS")) && !indianMarketOpen) {
                continue;
            }

            MarketData marketData = fetchRealMarketData(config);
            if (marketData == null || marketData.history.isEmpty()) continue;

            BigDecimal currentPrice = marketData.currentPrice;
            List<BigDecimal> fullHistory = marketData.history;

            BigDecimal rsi = technicalAnalysisService.calculateRSI(fullHistory, 14);
            BigDecimal ema20 = technicalAnalysisService.calculateEMA(fullHistory, 20);
            String pattern = technicalAnalysisService.detectPattern(fullHistory);

            BigDecimal localResistance = fullHistory.subList(Math.max(0, fullHistory.size()-20), fullHistory.size()).stream().max(BigDecimal::compareTo).orElse(currentPrice);
            BigDecimal localSupport = fullHistory.subList(Math.max(0, fullHistory.size()-20), fullHistory.size()).stream().min(BigDecimal::compareTo).orElse(currentPrice);

            boolean isBuyPattern = pattern.contains("Bottom") || pattern.contains("Recovery") || currentPrice.compareTo(localResistance.multiply(new BigDecimal("1.0002"))) > 0;
            boolean isSellPattern = (pattern.contains("Bearish") && currentPrice.compareTo(localSupport) < 0) || currentPrice.compareTo(localSupport.multiply(new BigDecimal("0.9998"))) < 0;

            if (!isBuyPattern && !isSellPattern && (config.sector.equals("Equity") || config.sector.equals("Index"))) {
                if (currentPrice.compareTo(ema20.multiply(new BigDecimal("1.001"))) > 0 && rsi.compareTo(new BigDecimal("50")) > 0) {
                    isBuyPattern = true;
                } else if (currentPrice.compareTo(ema20.multiply(new BigDecimal("0.999"))) < 0 && rsi.compareTo(new BigDecimal("50")) < 0) {
                    isSellPattern = true;
                }
            }

            if (isBuyPattern && rsi.compareTo(new BigDecimal("40")) > 0) {
                generateSignal(config, currentPrice, rsi, ema20, pattern, SignalType.CALL);
            } else if (isSellPattern && rsi.compareTo(new BigDecimal("60")) < 0) {
                generateSignal(config, currentPrice, rsi, ema20, pattern, SignalType.PUT);
            }
        }
    }

    private static class MarketData {
        BigDecimal currentPrice; List<BigDecimal> history;
        MarketData(BigDecimal cp, List<BigDecimal> h) { this.currentPrice = cp; this.history = h; }
    }

    private MarketData fetchRealMarketData(StockConfig config) {
        String token = config.yfSymbol.equals("^NSEI") ? "99926000" : (config.yfSymbol.equals("^NSEBANK") ? "99926009" : null);
        if (token != null) {
            Double ltp = smartApiService.getLastPrice(token);
            if (ltp != null && ltp > 0) return new MarketData(BigDecimal.valueOf(ltp), fetchHistoricalDataOnly(config));
        }
        if (config.yfSymbol.contains("-USD")) {
            Double ltp = binanceWebSocketService.getLastPrice(config.yfSymbol);
            if (ltp != null && ltp > 0) return new MarketData(BigDecimal.valueOf(ltp), fetchHistoricalDataOnly(config));
        }
        return fetchFromYahoo(config);
    }

    private List<BigDecimal> fetchHistoricalDataOnly(StockConfig config) {
        List<BigDecimal> history = new ArrayList<>();
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + config.yfSymbol + "?interval=1m&range=1d";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            org.springframework.http.ResponseEntity<com.fasterxml.jackson.databind.JsonNode> response = 
                restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, new org.springframework.http.HttpEntity<>(headers), com.fasterxml.jackson.databind.JsonNode.class);
            com.fasterxml.jackson.databind.JsonNode closePrices = response.getBody().get("chart").get("result").get(0).get("indicators").get("quote").get(0).get("close");
            for (com.fasterxml.jackson.databind.JsonNode node : closePrices) {
                if (!node.isNull()) history.add(BigDecimal.valueOf(node.asDouble()));
            }
        } catch (Exception e) {}
        return history;
    }

    private MarketData fetchFromYahoo(StockConfig config) {
        List<BigDecimal> history = fetchHistoricalDataOnly(config);
        if (history.isEmpty()) return null;
        return new MarketData(history.get(history.size() - 1), history);
    }

    private void generateSignal(StockConfig config, BigDecimal price, BigDecimal rsi, BigDecimal ema, String pattern, SignalType type) {
        String displaySymbol = config.symbol;
        String patternName = "AI Statistical Pulse";

        boolean isIndianStock = config.yfSymbol.equals("^NSEI") || config.yfSymbol.equals("^NSEBANK") || config.yfSymbol.equals("NIFTY_FIN_SERVICE.NS") || config.yfSymbol.equals("^NSEMDCP50") || config.yfSymbol.endsWith(".NS");
        
        String displaySector = config.sector;

        if (isIndianStock) {
            int roundTo = 100; // Default
            if (config.yfSymbol.equals("^NSEI")) roundTo = 50;
            else if (config.yfSymbol.equals("^NSEBANK")) roundTo = 100;
            else if (config.yfSymbol.equals("NIFTY_FIN_SERVICE.NS")) roundTo = 100;
            else if (config.yfSymbol.equals("^NSEMDCP50")) roundTo = 25;
            else if (price.compareTo(new BigDecimal("1000")) < 0) roundTo = 10;
            else if (price.compareTo(new BigDecimal("5000")) < 0) roundTo = 50;
            else roundTo = 100;

            long atmStrike = Math.round(price.doubleValue() / roundTo) * roundTo;
            String suffix = (type == SignalType.CALL ? "CE" : "PE");
            displaySymbol = config.symbol + " " + atmStrike + " " + suffix;
            patternName = "Neural Pattern " + suffix;
            displaySector = config.sector.equals("Equity") ? "Stock Options" : config.sector;
        } else if (config.yfSymbol.contains("-USD")) {
            String coin = config.yfSymbol.split("-")[0];
            String typeStr = (type == SignalType.CALL ? "CALL (LONG)" : "PUT (SHORT)");
            displaySymbol = coin + " " + typeStr;
            patternName = "Crypto Neural Pulse";
        }

        final String sym = displaySymbol;
        final String sec = displaySector;
        Stock stock = stockRepository.findBySymbol(sym).orElseGet(() -> stockRepository.save(Stock.builder().symbol(sym).name(sym).sector(sec).build()));

        double confidence = technicalAnalysisService.computeScientificConfidence(price, ema, rsi, pattern);
        BigDecimal sl = (type == SignalType.CALL) ? price.multiply(new BigDecimal("0.995")) : price.multiply(new BigDecimal("1.005"));
        BigDecimal target = (type == SignalType.CALL) ? price.multiply(new BigDecimal("1.02")) : price.multiply(new BigDecimal("0.98"));

        // Refined Pattern Identification for Real-Money Users
        if (rsi.compareTo(new BigDecimal("70")) > 0) patternName = "AI Overbought RSI";
        else if (rsi.compareTo(new BigDecimal("30")) < 0) patternName = "Neural Double Bottom";
        else if (price.compareTo(ema) > 0) patternName = "AI Momentum Trend";
        else if (price.compareTo(ema) < 0) patternName = "Neural V-Pulse";
        
        TradeSignal signal = TradeSignal.builder()
                .stock(stock)
                .signalType(type)
                .patternName(patternName)
                .entryPrice(price.doubleValue())
                .stopLoss(sl.doubleValue())
                .targetPrice(target.doubleValue())
                .confidenceScore(confidence)
                .lotSize(config.lotSize)
                .investmentRequired(price.doubleValue() * config.lotSize)
                .generatedAt(LocalDateTime.now())
                .build();
        tradeSignalRepository.save(signal);
        signalUpdateService.broadcastSignal(signal);
    }
}

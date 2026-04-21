package com.tradeintel.controller;

import com.tradeintel.entity.SignalType;
import com.tradeintel.entity.Stock;
import com.tradeintel.entity.TradeSignal;
import com.tradeintel.repository.StockRepository;
import com.tradeintel.repository.TradeSignalRepository;
import com.tradeintel.service.SignalUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Direct TradingView Webhook Connector.
 * Enables millisecond delivery of signals from TradingView Alerts to the Dashboard.
 */
@RestController
@RequestMapping("/api/webhooks/tradingview")
@RequiredArgsConstructor
@Slf4j
public class TradingViewWebhookController {

    private final TradeSignalRepository tradeSignalRepository;
    private final StockRepository stockRepository;
    private final SignalUpdateService signalUpdateService;

    @GetMapping("/signal")
    public Map<String, String> getStatus() {
        return Map.of("status", "Active", "message", "TradingView Webhook is ready. Please use POST for signals.");
    }

    @PostMapping("/signal")
    public Map<String, String> receiveSignal(@RequestBody Map<String, Object> payload) {
        log.info("🔔 Ultra-Fast Direct Signal: {}", payload);

        try {
            String symbol = (String) payload.getOrDefault("symbol", "UNKNOWN");
            String typeStr = (String) payload.getOrDefault("type", "CALL");
            String pattern = (String) payload.getOrDefault("pattern", "TV Dynamic Alarm");
            
            // Extracting all values directly from TradingView Alert Message
            Double price = Double.valueOf(payload.getOrDefault("price", "0.0").toString());
            Double stopLoss = Double.valueOf(payload.getOrDefault("stopLoss", price * 0.99).toString());
            Double target = Double.valueOf(payload.getOrDefault("target", price * 1.01).toString());
            Double confidence = Double.valueOf(payload.getOrDefault("confidence", "90.0").toString());
            Integer lotSize = Integer.valueOf(payload.getOrDefault("lotSize", "1").toString());
            
            SignalType signalType = "PUT".equalsIgnoreCase(typeStr) ? SignalType.PUT : SignalType.CALL;

            Stock stock = stockRepository.findBySymbol(symbol)
                    .orElseGet(() -> stockRepository.save(Stock.builder()
                            .symbol(symbol)
                            .name(symbol)
                            .sector("Real-time Feed")
                            .build()));

            TradeSignal signal = TradeSignal.builder()
                    .stock(stock)
                    .signalType(signalType)
                    .patternName(pattern)
                    .entryPrice(price)
                    .stopLoss(stopLoss)
                    .targetPrice(target)
                    .confidenceScore(confidence)
                    .lotSize(lotSize)
                    .investmentRequired(price * lotSize)
                    .generatedAt(LocalDateTime.now())
                    .build();

            tradeSignalRepository.save(signal);
            signalUpdateService.broadcastSignal(signal);
            return Map.of("status", "success", "message", "Milli-second Signal Recorded Dynamically");

        } catch (Exception e) {
            log.error("❌ Signal Extraction Failed: {}", e.getMessage());
            return Map.of("status", "error", "message", "Missing or invalid parameters in TV payload");
        }
    }
}

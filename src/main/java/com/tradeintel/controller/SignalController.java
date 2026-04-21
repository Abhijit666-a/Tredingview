package com.tradeintel.controller;

import com.tradeintel.entity.TradeSignal;
import com.tradeintel.entity.SignalType;
import com.tradeintel.repository.TradeSignalRepository;
import com.tradeintel.service.SignalUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * REST Controller exposing Trade Signal endpoints for the Angular Dashboard.
 * 100% Real-time signals without dummy data.
 */
@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
@Slf4j
public class SignalController {

    private final TradeSignalRepository tradeSignalRepository;
    private final SignalUpdateService signalUpdateService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSignals() {
        return signalUpdateService.subscribe();
    }

    /**
     * GET /api/signals
     * Returns the latest trade signals from the real-time engine.
     */
    @GetMapping
    public ResponseEntity<?> getAllSignals() {
        try {
            log.info("📡 Fetching latest real-time signals...");
            List<TradeSignal> signals = tradeSignalRepository.findTop10ByOrderByGeneratedAtDesc();
            
            if (signals.isEmpty()) {
                return ResponseEntity.ok(List.of()); // Return empty list instead of dummy data
            }
            
            return ResponseEntity.ok(signals);
        } catch (Exception e) {
            log.error("❌ Signal Fetch Error: ", e);
            return ResponseEntity.internalServerError().body("Engine Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeSignal> getSignalById(@PathVariable Long id) {
        return tradeSignalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{signalType}")
    public ResponseEntity<List<TradeSignal>> getSignalsByType(@PathVariable SignalType signalType) {
        List<TradeSignal> signals = tradeSignalRepository.findBySignalTypeOrderByGeneratedAtDesc(signalType);
        return ResponseEntity.ok(signals);
    }
}

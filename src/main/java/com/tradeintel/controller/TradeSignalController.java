package com.tradeintel.controller;

import com.tradeintel.dto.TradeSignalDTO;
import com.tradeintel.mapper.EntityMapper;
import com.tradeintel.repository.TradeSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Trade Signal operations.
 *
 * <p>Provides endpoints to query generated trade signals.</p>
 */
@RestController
@RequestMapping("/api/v1/signals")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class TradeSignalController {

    private final TradeSignalRepository tradeSignalRepository;
    private final EntityMapper entityMapper;

    /**
     * GET /api/v1/signals — Retrieves the latest 10 trade signals.
     */
    @GetMapping
    public ResponseEntity<List<TradeSignalDTO>> getLatestSignals() {
        log.info("📡 API Request — GET /api/v1/signals");

        List<TradeSignalDTO> signals = tradeSignalRepository.findTop10ByOrderByGeneratedAtDesc()
                .stream()
                .map(entityMapper::toTradeSignalDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(signals);
    }

    /**
     * GET /api/v1/signals/{id} — Retrieves a specific trade signal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TradeSignalDTO> getSignalById(@PathVariable Long id) {
        log.info("📡 API Request — GET /api/v1/signals/{}", id);

        return tradeSignalRepository.findById(id)
                .map(entityMapper::toTradeSignalDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/signals/stock/{symbol} — Retrieves signals for a specific stock.
     */
    @GetMapping("/stock/{symbol}")
    public ResponseEntity<List<TradeSignalDTO>> getSignalsByStock(@PathVariable String symbol) {
        log.info("📡 API Request — GET /api/v1/signals/stock/{}", symbol);

        List<TradeSignalDTO> signals = tradeSignalRepository
                .findByStockSymbolOrderByGeneratedAtDesc(symbol)
                .stream()
                .map(entityMapper::toTradeSignalDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(signals);
    }
}

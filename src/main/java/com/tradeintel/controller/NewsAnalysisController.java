package com.tradeintel.controller;

import com.tradeintel.dto.NewsAnalysisDTO;
import com.tradeintel.mapper.EntityMapper;
import com.tradeintel.repository.NewsAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for News Analysis operations.
 *
 * <p>Provides endpoints to query AI-powered news sentiment analyses.</p>
 */
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "https://tredingview.onrender.com"})
public class NewsAnalysisController {

    private final NewsAnalysisRepository newsAnalysisRepository;
    private final EntityMapper entityMapper;

    /**
     * GET /api/v1/news — Retrieves the latest 10 news analyses.
     */
    @GetMapping
    public ResponseEntity<List<NewsAnalysisDTO>> getLatestNews() {
        log.info("📡 API Request — GET /api/v1/news");

        List<NewsAnalysisDTO> analyses = newsAnalysisRepository.findTop10ByOrderByAnalyzedAtDesc()
                .stream()
                .map(entityMapper::toNewsAnalysisDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(analyses);
    }

    /**
     * GET /api/v1/news/stock/{symbol} — Retrieves news analyses for a specific stock.
     */
    @GetMapping("/stock/{symbol}")
    public ResponseEntity<List<NewsAnalysisDTO>> getNewsByStock(@PathVariable String symbol) {
        log.info("📡 API Request — GET /api/v1/news/stock/{}", symbol);

        List<NewsAnalysisDTO> analyses = newsAnalysisRepository
                .findByStockSymbolOrderByAnalyzedAtDesc(symbol)
                .stream()
                .map(entityMapper::toNewsAnalysisDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(analyses);
    }
}

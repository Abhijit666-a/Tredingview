package com.tradeintel.mapper;

import com.tradeintel.dto.NewsAnalysisDTO;
import com.tradeintel.dto.TradeSignalDTO;
import com.tradeintel.entity.NewsAnalysis;
import com.tradeintel.entity.TradeSignal;
import org.springframework.stereotype.Component;

/**
 * Mapper utility for converting between Entity and DTO objects.
 */
@Component
public class EntityMapper {

    /**
     * Converts a {@link TradeSignal} entity to a {@link TradeSignalDTO}.
     */
    public TradeSignalDTO toTradeSignalDTO(TradeSignal signal) {
        return TradeSignalDTO.builder()
                .id(signal.getId())
                .stockSymbol(signal.getStock().getSymbol())
                .stockName(signal.getStock().getName())
                .signalType(signal.getSignalType())
                .patternName(signal.getPatternName())
                .entryPrice(signal.getEntryPrice())
                .stopLoss(signal.getStopLoss())
                .targetPrice(signal.getTargetPrice())
                .confidenceScore(signal.getConfidenceScore())
                .generatedAt(signal.getGeneratedAt())
                .build();
    }

    /**
     * Converts a {@link NewsAnalysis} entity to a {@link NewsAnalysisDTO}.
     */
    public NewsAnalysisDTO toNewsAnalysisDTO(NewsAnalysis analysis) {
        return NewsAnalysisDTO.builder()
                .id(analysis.getId())
                .stockSymbol(analysis.getStock().getSymbol())
                .headline(analysis.getHeadline())
                .sentimentScore(analysis.getSentimentScore())
                .aiSummary(analysis.getAiSummary())
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
}

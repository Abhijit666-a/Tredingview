package com.tradeintel.dto;

import com.tradeintel.entity.SignalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for exposing TradeSignal data via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeSignalDTO {

    private Long id;
    private String stockSymbol;
    private String stockName;
    private SignalType signalType;
    private String patternName;
    private Double entryPrice;
    private Double stopLoss;
    private Double targetPrice;
    private Double confidenceScore;
    private LocalDateTime generatedAt;
}

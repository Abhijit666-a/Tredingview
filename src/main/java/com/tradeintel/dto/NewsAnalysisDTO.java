package com.tradeintel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for exposing NewsAnalysis data via REST API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsAnalysisDTO {

    private Long id;
    private String stockSymbol;
    private String headline;
    private Double sentimentScore;
    private String aiSummary;
    private LocalDateTime analyzedAt;
}

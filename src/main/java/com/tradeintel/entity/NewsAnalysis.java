package com.tradeintel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing AI-powered news sentiment analysis for a stock.
 */
@Entity
@Table(name = "news_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id", nullable = false)
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Stock stock;

    @Column(nullable = false, length = 500)
    private String headline;

    /**
     * Sentiment score between -1.0 (very bearish) and 1.0 (very bullish).
     */
    @Column(nullable = false)
    private Double sentimentScore;

    /**
     * AI-generated summary of the news impact on the stock.
     */
    @Column(length = 1000)
    private String aiSummary;

    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
}

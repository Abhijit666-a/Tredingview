package com.tradeintel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a generated trade signal (CALL or PUT).
 * A signal is produced when both technical and sentiment conditions are met.
 */
@Entity
@Table(name = "trade_signals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "stock")
@EqualsAndHashCode(exclude = "stock")
public class TradeSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SignalType signalType;

    @Column(nullable = false, length = 80)
    private String patternName;

    @Column(nullable = false)
    private Double entryPrice;

    @Column(nullable = false)
    private Double stopLoss;

    @Column(nullable = false)
    private Double targetPrice;

    /**
     * Confidence score between 0.0 and 1.0 indicating signal strength.
     */
    @Column(nullable = false)
    private Double confidenceScore;

    // Financial columns for Lot Calculations
    private Integer lotSize;
    private Double investmentRequired;
    private Double potentialProfit;
    private Double potentialLoss;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }
    }
}

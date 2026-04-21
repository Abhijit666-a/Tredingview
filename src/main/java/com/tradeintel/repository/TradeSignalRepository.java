package com.tradeintel.repository;

import com.tradeintel.entity.SignalType;
import com.tradeintel.entity.TradeSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for TradeSignal entity CRUD and query operations.
 */
@Repository
public interface TradeSignalRepository extends JpaRepository<TradeSignal, Long> {

    List<TradeSignal> findByStockIdOrderByGeneratedAtDesc(Long stockId);

    List<TradeSignal> findBySignalTypeOrderByGeneratedAtDesc(SignalType signalType);

    List<TradeSignal> findByStockSymbolOrderByGeneratedAtDesc(String symbol);

    List<TradeSignal> findByGeneratedAtAfterOrderByGeneratedAtDesc(LocalDateTime after);

    List<TradeSignal> findTop10ByOrderByGeneratedAtDesc();
}

package com.tradeintel.repository;

import com.tradeintel.entity.NewsAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for NewsAnalysis entity CRUD and query operations.
 */
@Repository
public interface NewsAnalysisRepository extends JpaRepository<NewsAnalysis, Long> {

    List<NewsAnalysis> findByStockIdOrderByAnalyzedAtDesc(Long stockId);

    List<NewsAnalysis> findByStockSymbolOrderByAnalyzedAtDesc(String symbol);

    List<NewsAnalysis> findTop10ByOrderByAnalyzedAtDesc();
}

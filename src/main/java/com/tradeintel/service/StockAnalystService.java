package com.tradeintel.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockAnalystService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getMonthlyAnalysis(String yfSymbol) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yfSymbol + "?interval=1d&range=1mo";
            
            // Yahoo Finance often requires a User-Agent header
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, JsonNode.class);
            JsonNode response = responseEntity.getBody();
            
            if (response != null && response.has("chart") && !response.get("chart").get("result").isNull()) {
                JsonNode result = response.get("chart").get("result").get(0);
                JsonNode indicators = result.get("indicators").get("quote").get(0);
                
                if (indicators.has("close")) {
                    JsonNode prices = indicators.get("close");
                    List<Double> historicalPrices = new ArrayList<>();
                    for (JsonNode node : prices) {
                        if (node != null && !node.isNull()) historicalPrices.add(node.asDouble());
                    }

                    if (historicalPrices.size() > 1) {
                        double firstPrice = historicalPrices.get(0);
                        double lastPrice = historicalPrices.get(historicalPrices.size() - 1);
                        double monthlyReturn = ((lastPrice - firstPrice) / firstPrice) * 100;
                        String recommendation = monthlyReturn > 5 ? "STRONG BUY" : (monthlyReturn > 0 ? "HOLD" : "AVOID");
                        
                        return Map.of(
                            "symbol", yfSymbol,
                            "monthlyReturn", monthlyReturn,
                            "recommendation", recommendation,
                            "success", true
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed monthly analysis for {}: {}", yfSymbol, e.getMessage());
        }
        return Map.of("symbol", yfSymbol, "monthlyReturn", 0.0, "recommendation", "FETCHING...", "success", false);
    }

    private double calculateVolatility(List<Double> prices) {
        // Simple standard deviation concept
        double mean = prices.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = prices.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }
}

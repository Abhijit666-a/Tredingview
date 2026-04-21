package com.tradeintel.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundamentalService {

    private final RestTemplate restTemplate;
    private final TechnicalAnalysisService technicalAnalysisService;

    public Map<String, Object> getCompanyHealth(String yfSymbol) {
        try {
            // Using v8 chart endpoint for better stability across all symbols
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yfSymbol + "?interval=1d&range=1mo";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode root = responseEntity.getBody();

            if (root != null && root.has("chart") && !root.get("chart").get("result").isNull()) {
                JsonNode result = root.get("chart").get("result").get(0);
                JsonNode meta = result.get("meta");

                Map<String, Object> health = new HashMap<>();
                health.put("symbol", yfSymbol);
                health.put("price", String.format("%.2f", meta.get("regularMarketPrice").asDouble()));
                
                // Indicators calculation
                JsonNode closePrices = result.get("indicators").get("quote").get(0).get("close");
                List<BigDecimal> history = new ArrayList<>();
                for (JsonNode node : closePrices) {
                    if (!node.isNull()) history.add(BigDecimal.valueOf(node.asDouble()));
                }

                if (!history.isEmpty()) {
                    BigDecimal rsi = technicalAnalysisService.calculateRSI(history, 14);
                    BigDecimal sma20 = technicalAnalysisService.calculateSMA(history, 20);
                    health.put("rsi", rsi);
                    health.put("sma20", sma20);
                    
                    double score = rsi.doubleValue() > 60 ? 85 : 55; // Simple health scoring
                    health.put("healthScore", score);
                    health.put("rating", score > 80 ? "EXCELLENT" : "STABLE");
                }
                
                return health;
            }
        } catch (Exception e) {
            log.error("❌ Fundamental Fetch Failed for {}: {}", yfSymbol, e.getMessage());
        }
        return Map.of("symbol", yfSymbol, "error", "LIVE_FETCH_FAILED");
    }

    public Map<String, Object> getSimpleQuote(String symbol) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1m&range=1d";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode result = response.getBody().get("chart").get("result").get(0);
            JsonNode meta = result.get("meta");

            double price = meta.get("regularMarketPrice").asDouble();
            double prevClose = meta.get("previousClose").asDouble();
            double high = meta.has("regularMarketDayHigh") ? meta.get("regularMarketDayHigh").asDouble() : price;
            double low = meta.has("regularMarketDayLow") ? meta.get("regularMarketDayLow").asDouble() : price;
            
            double changeVal = price - prevClose;
            double changePercent = (prevClose != 0) ? (changeVal / prevClose) * 100 : 0.0;

            Map<String, Object> quote = new HashMap<>();
            quote.put("symbol", symbol);
            quote.put("name", getFriendlyName(symbol));
            quote.put("price", price);
            quote.put("high", high);
            quote.put("low", low);
            quote.put("change", changeVal);
            quote.put("changePercent", changePercent);
            quote.put("time", new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
            quote.put("trend", changeVal >= 0 ? "up" : "down");
            
            return quote;
        } catch (Exception e) {
            log.error("❌ Quote API Error for {}: {}", symbol, e.getMessage());
            return Map.of("symbol", symbol, "price", 0.0, "change", 0.0, "trend", "down");
        }
    }

    public List<Map<String, Object>> getOptionChain(String symbol) {
        double currentPrice = 0.0;
        int step = 50;
        if (symbol.equalsIgnoreCase("^NSEBANK") || symbol.equalsIgnoreCase("NIFTY_FIN_SERVICE.NS")) step = 100;
        else if (symbol.equalsIgnoreCase("^NSEMDCP50")) step = 25;
        else step = 50;
        
        // Get current Index Price
        Map<String, Object> quote = getSimpleQuote(symbol);
        if (quote != null && quote.containsKey("price")) {
            currentPrice = (double) quote.get("price");
        }
        
        if (currentPrice == 0) return new ArrayList<>();

        long atm = Math.round(currentPrice / step) * step;
        List<Map<String, Object>> chain = new ArrayList<>();
        
        for (int i = -5; i <= 5; i++) {
            long strike = atm + (i * step);
            Map<String, Object> row = new HashMap<>();
            row.put("strike", strike);
            
            // Mocking premium based on moneyness for immediate display
            double diff = currentPrice - strike;
            double cePrice = Math.max(5, (diff > 0 ? diff * 1.1 : 200 / (Math.abs(diff/step) + 1)));
            double pePrice = Math.max(5, (diff < 0 ? Math.abs(diff) * 1.1 : 200 / (Math.abs(diff/step) + 1)));
            
            row.put("ceLtp", cePrice);
            row.put("peLtp", pePrice);
            row.put("isAtm", strike == atm);
            chain.add(row);
        }
        return chain;
    }

    private String getFriendlyName(String symbol) {
        if (symbol.equals("^NSEI")) return "NIFTY 50";
        if (symbol.equals("^NSEBANK")) return "BANK NIFTY";
        if (symbol.equals("NIFTY_FIN_SERVICE.NS")) return "FIN NIFTY";
        if (symbol.equals("^NSEMDCP50")) return "MIDCAP NIFTY";
        if (symbol.equals("^BSESN")) return "BSE SENSEX";
        return symbol.replace(".NS", "").replace("^", "");
    }
}

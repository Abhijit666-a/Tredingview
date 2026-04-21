package com.tradeintel.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clean Binance Integration (SDK-Free).
 * Uses direct REST calls for zero-dependency stability.
 */
@Service
@Slf4j
public class BinanceWebSocketService {

    private final Map<String, Double> cryptoPriceCache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final SignalUpdateService signalUpdateService;

    @org.springframework.beans.factory.annotation.Autowired
    public BinanceWebSocketService(SignalUpdateService signalUpdateService) {
        this.signalUpdateService = signalUpdateService;
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    @PostConstruct
    public void init() {
        log.info("🪙 Establishing Direct Binance Connection (SDK-Free Polling)...");
        fetchLivePrices();
    }

    @Scheduled(fixedRate = 1000)
    public void fetchLivePrices() {
        updatePrice("btcusdt", "BTC-USD");
        updatePrice("ethusdt", "ETH-USD");
    }

    private void updatePrice(String binanceSymbol, String displaySymbol) {
        try {
            // Use api1.binance.com or api-gcp.binance.com as fallback if api.binance.com is blocked
            String url = "https://api1.binance.com/api/v3/ticker/price?symbol=" + binanceSymbol.toUpperCase();
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null) {
                JSONObject json = new JSONObject(response);
                double price = json.getDouble("price");
                cryptoPriceCache.put(displaySymbol, price);
                signalUpdateService.broadcastPriceTick(displaySymbol, price);
                log.debug("🪙 BINANCE-LTP: {} -> {}", displaySymbol, price);
            }
        } catch (Exception e) {
            log.error("❌ Binance API Error for {}: {}", displaySymbol, e.getMessage());
        }
    }

    public Double getLastPrice(String symbol) {
        return cryptoPriceCache.get(symbol);
    }
}

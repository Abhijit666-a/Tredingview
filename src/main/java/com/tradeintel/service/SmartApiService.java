package com.tradeintel.service;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clean SmartAPI Integration (SDK-Free).
 * Uses direct REST calls to bypass Maven/JitPack issues.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmartApiService {

    @Value("${tradeintel.smartapi.client-id}")
    private String clientId;

    @Value("${tradeintel.smartapi.password}")
    private String password;

    @Value("${tradeintel.smartapi.api-key}")
    private String apiKey;

    @Value("${tradeintel.smartapi.totp-secret}")
    private String totpSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken = null;
    private final Map<String, Double> ltpCache = new ConcurrentHashMap<>();
    private final SignalUpdateService signalUpdateService;

    @PostConstruct
    public void init() {
        log.info("🤖 Establishing Direct SmartAPI Connection (SDK-Free Mode)...");
        login();
    }

    public void login() {
        try {
            // 1. Generate TOTP
            DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
            String totpCode = codeGenerator.generate(totpSecret, Instant.now().getEpochSecond() / 30);
            
            // 2. Prepare Login Request
            String url = "https://apiconnect.angelbroking.com/rest/auth/angelbroking/user/v1/loginByPassword";
            
            JSONObject body = new JSONObject();
            body.put("clientcode", clientId);
            body.put("password", password);
            body.put("totp", totpCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-PrivateKey", apiKey);
            headers.set("Accept", "application/json");
            headers.set("X-UserType", "USER");
            headers.set("X-SourceID", "WEB");
            headers.set("X-ClientLocalIP", "127.0.0.1");
            headers.set("X-ClientPublicIP", "106.193.147.98");
            headers.set("X-MACAddress", "fe80::216e:6507:4b90:3719%18");

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                if (jsonResponse.getBoolean("status")) {
                    this.accessToken = jsonResponse.getJSONObject("data").getString("jwtToken");
                    log.info("✅ Direct SmartAPI Session Active! (SDK-Free)");
                } else {
                    log.error("❌ SmartAPI Login Failed: {}", jsonResponse.getString("message"));
                }
            }
        } catch (Exception e) {
            log.error("❌ Direct Logic Error: {}", e.getMessage());
        }
    }

    /**
     * High-Frequency Price Fetcher (1s).
     * Replaces WebSocket for zero-dependency stability.
     */
    @Scheduled(fixedRate = 1000)
    public void fetchLivePrices() {
        if (accessToken == null) return;

        fetchLtp("99926000", "NSE", "NIFTY");
        fetchLtp("99926009", "NSE", "BANKNIFTY");
    }

    private void fetchLtp(String token, String exchange, String symbol) {
        try {
            String url = "https://apiconnect.angelbroking.com/order-service/rest/secure/angelbroking/order/v1/getLtpData";
            
            JSONObject body = new JSONObject();
            body.put("exchange", exchange);
            body.put("tradingsymbol", symbol);
            body.put("symboltoken", token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("X-PrivateKey", apiKey);
            headers.set("Accept", "application/json");
            headers.set("X-UserType", "USER");
            headers.set("X-SourceID", "WEB");
            headers.set("X-ClientLocalIP", "127.0.0.1");
            headers.set("X-MACAddress", "fe80::216e:6507:4b90:3719%18");

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JSONObject json = new JSONObject(response.getBody());

            if (json.has("status") && json.getBoolean("status")) {
                if (json.has("data") && !json.isNull("data")) {
                    double ltp = json.getJSONObject("data").getDouble("ltp");
                    ltpCache.put(token, ltp);
                    signalUpdateService.broadcastPriceTick(symbol, ltp);
                    log.debug("⚡ SMART-LTP: {} -> {}", symbol, ltp);
                }
            } else {
                log.warn("⚠️ SmartAPI LTP Offline for {}: {}", symbol, json.optString("message", "Market Closed/Invalid JSON"));
            }
        } catch (Exception e) {
            log.error("❌ LTP Fetch Failed for {}: {}", symbol, e.getMessage());
        }
    }

    public Double getLastPrice(String token) {
        return ltpCache.get(token);
    }
}

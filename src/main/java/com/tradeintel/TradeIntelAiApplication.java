package com.tradeintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TradeIntel AI - Main Application Entry Point
 *
 * Real-time trade signal engine powered by Technical Analysis
 * and AI-driven News Sentiment Analysis.
 */
@SpringBootApplication
@EnableScheduling
public class TradeIntelAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeIntelAiApplication.class, args);
    }
}

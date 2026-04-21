package com.tradeintel.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import java.util.List;

/**
 * Advanced Neural Technical Analysis Service.
 * Implements statistical models and pattern recognition for trade confirmation.
 */
@Service
public class TechnicalAnalysisService {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    public BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices.size() <= period) return new BigDecimal("50.00");
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;

        for (int i = 1; i <= period; i++) {
            BigDecimal diff = prices.get(i).subtract(prices.get(i - 1));
            if (diff.compareTo(BigDecimal.ZERO) > 0) avgGain = avgGain.add(diff);
            else avgLoss = avgLoss.add(diff.abs());
        }

        avgGain = avgGain.divide(BigDecimal.valueOf(period), MC);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), MC);

        for (int i = period + 1; i < prices.size(); i++) {
            BigDecimal diff = prices.get(i).subtract(prices.get(i - 1));
            BigDecimal gain = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
            BigDecimal loss = diff.compareTo(BigDecimal.ZERO) < 0 ? diff.abs() : BigDecimal.ZERO;

            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1)).add(gain).divide(BigDecimal.valueOf(period), MC);
            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1)).add(loss).divide(BigDecimal.valueOf(period), MC);
        }

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) return new BigDecimal("100.00");
        BigDecimal rs = avgGain.divide(avgLoss, MC);
        return BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), MC)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        BigDecimal sum = prices.subList(prices.size() - period, prices.size()).stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(period), MC).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return calculateSMA(prices, period);
        BigDecimal alpha = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(period + 1), MC);
        BigDecimal ema = calculateSMA(prices.subList(0, period), period);
        for (int i = period; i < prices.size(); i++) {
            ema = prices.get(i).subtract(ema).multiply(alpha).add(ema);
        }
        return ema.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateVolatility(List<BigDecimal> prices, int period) {
        if (prices.size() < period) return BigDecimal.ZERO;
        BigDecimal sma = calculateSMA(prices, period);
        BigDecimal varianceSum = BigDecimal.ZERO;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            varianceSum = varianceSum.add(prices.get(i).subtract(sma).pow(2));
        }
        return BigDecimal.valueOf(Math.sqrt(varianceSum.divide(BigDecimal.valueOf(period), MC).doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Strategic Pattern Recognition Engine.
     */
    public String detectPattern(List<BigDecimal> prices) {
        if (prices.size() < 20) return "Momentum Scan";
        BigDecimal current = prices.get(prices.size() - 1);
        BigDecimal prev = prices.get(prices.size() - 2);
        
        // 1. Double Bottom Detection
        int size = prices.size();
        BigDecimal min1 = prices.subList(size-20, size-10).stream().min(BigDecimal::compareTo).orElse(current);
        BigDecimal min2 = prices.subList(size-10, size).stream().min(BigDecimal::compareTo).orElse(current);
        if (min1.subtract(min2).abs().compareTo(current.multiply(new BigDecimal("0.001"))) < 0 && current.compareTo(prev) > 0) {
            return "Neural Double Bottom";
        }
        // 2. V-Shape Recovery
        if (current.compareTo(prev) > 0 && prev.compareTo(prices.get(size-3)) < 0) return "V-Pulse Recovery";

        return current.compareTo(prev) > 0 ? "Bullish Trend" : "Bearish Trend";
    }

    public BigDecimal calculateStopLoss(BigDecimal entryPrice, double percentage) {
        BigDecimal factor = BigDecimal.valueOf(100 - percentage).divide(new BigDecimal("100"), MC);
        return entryPrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTarget(BigDecimal entryPrice, BigDecimal stopLoss, double riskRewardRatio) {
        BigDecimal risk = entryPrice.subtract(stopLoss);
        BigDecimal reward = risk.multiply(BigDecimal.valueOf(riskRewardRatio));
        return entryPrice.add(reward).setScale(2, RoundingMode.HALF_UP);
    }

    public double computeScientificConfidence(BigDecimal price, BigDecimal sma, BigDecimal rsi, String pattern) {
        double score = 0.5;
        if (rsi.compareTo(new BigDecimal("65")) > 0) score += 0.2;
        if (rsi.compareTo(new BigDecimal("35")) < 0) score += 0.2;
        if (pattern.contains("Double") || pattern.contains("Recovery")) score += 0.25;
        if (price.compareTo(sma) > 0) score += 0.05;
        return Math.min(0.99, Math.max(0.1, score));
    }
}

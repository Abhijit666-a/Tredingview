package com.tradeintel;

import com.tradeintel.service.TechnicalAnalysisService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TechnicalAnalysisServiceTest {

    private final TechnicalAnalysisService service = new TechnicalAnalysisService();

    @Test
    void testCalculateSMA() {
        List<BigDecimal> prices = Arrays.asList(
            new BigDecimal("100"), new BigDecimal("110"), new BigDecimal("120"), 
            new BigDecimal("130"), new BigDecimal("140")
        );
        BigDecimal sma = service.calculateSMA(prices, 5);
        assertEquals(new BigDecimal("120.00"), sma);
    }

    @Test
    void testCalculateRSISimple() {
        List<BigDecimal> prices = Arrays.asList(
            new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("110"), 
            new BigDecimal("115"), new BigDecimal("120")
        );
        BigDecimal rsi = service.calculateRSI(prices, 4);
        assertTrue(rsi.compareTo(new BigDecimal("50")) > 0);
    }

    @Test
    void testStopLossAndTarget() {
        BigDecimal entry = new BigDecimal("1000.00");
        BigDecimal sl = service.calculateStopLoss(entry, 2.0); // 2% SL
        BigDecimal target = service.calculateTarget(entry, sl, 2.0); // 1:2 RR

        assertEquals(new BigDecimal("980.00"), sl);
        assertEquals(new BigDecimal("1040.00"), target);
    }
}

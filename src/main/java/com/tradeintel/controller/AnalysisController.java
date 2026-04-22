package com.tradeintel.controller;

import com.tradeintel.service.FundamentalService;
import com.tradeintel.service.StockAnalystService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://tredingview.onrender.com"})
public class AnalysisController {

    private final StockAnalystService analystService;
    private final FundamentalService fundamentalService;
    private final com.tradeintel.service.TradeSignalEngine tradeSignalEngine;

    private static final List<String> SYMBOLS = List.of("^NSEI", "^NSEBANK", "NIFTY_FIN_SERVICE.NS", "^NSEMDCP50", "BTC-USD", "ETH-USD");

    @PostMapping("/scan")
    public void submitForScan(@RequestParam String symbol) {
        String yfSymbol = symbol;
        if (!symbol.startsWith("^") && !symbol.contains("-USD") && !symbol.endsWith(".NS")) {
            yfSymbol = symbol + ".NS"; // Default to NSE for stocks
        }
        tradeSignalEngine.addSymbolToScan(symbol, "User Defined", yfSymbol, 1);
    }

    @GetMapping("/monthly-report")
    public List<Map<String, Object>> getMonthlyReport() {
        return SYMBOLS.stream()
                .map(analystService::getMonthlyAnalysis)
                .collect(Collectors.toList());
    }

    @GetMapping("/health/{symbol}")
    public Map<String, Object> getCompanyHealth(@PathVariable String symbol) {
        return fundamentalService.getCompanyHealth(symbol);
    }

    @GetMapping("/quotes")
    public List<Map<String, Object>> getMarketQuotes(@RequestParam String symbols) {
        return Arrays.stream(symbols.split(","))
                .map(fundamentalService::getSimpleQuote)
                .collect(Collectors.toList());
    }

    @GetMapping("/option-chain")
    public List<Map<String, Object>> getOptionChain(@RequestParam String symbol) {
        return fundamentalService.getOptionChain(symbol);
    }
}

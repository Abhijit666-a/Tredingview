package com.tradeintel.config;

import com.tradeintel.entity.SignalType;
import com.tradeintel.entity.Stock;
import com.tradeintel.entity.TradeSignal;
import com.tradeintel.repository.StockRepository;
import com.tradeintel.repository.TradeSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StockRepository stockRepository;
    private final TradeSignalRepository tradeSignalRepository;

    @Override
    public void run(String... args) {
        if (stockRepository.count() == 0) {
            log.info("🌱 Seeding initial market data...");

            Stock nifty = stockRepository.save(Stock.builder().symbol("NIFTY").name("Nifty 50").sector("Index").build());
            Stock btc = stockRepository.save(Stock.builder().symbol("BTC-USD").name("Bitcoin").sector("Crypto").build());
            Stock reliance = stockRepository.save(Stock.builder().symbol("RELIANCE").name("Reliance Industries").sector("Equity").build());
            Stock tcs = stockRepository.save(Stock.builder().symbol("TCS").name("Tata Consultancy Services").sector("Equity").build());
            Stock infy = stockRepository.save(Stock.builder().symbol("INFY").name("Infosys").sector("Equity").build());
            Stock eicher = stockRepository.save(Stock.builder().symbol("EICHERMOT").name("Eicher Motors").sector("Equity").build());
            Stock banknifty = stockRepository.save(Stock.builder().symbol("BANKNIFTY").name("Bank Nifty").sector("Index").build());
            
            log.info("Stocks created: {}, {}, {}, {}, {}, {}", btc.getSymbol(), reliance.getSymbol(), tcs.getSymbol(), infy.getSymbol(), eicher.getSymbol(), banknifty.getSymbol());

            // Seed index signal
            tradeSignalRepository.save(TradeSignal.builder()
                    .stock(nifty)
                    .signalType(SignalType.CALL)
                    .patternName("Neural Pattern CE")
                    .entryPrice(22500.0)
                    .stopLoss(22450.0)
                    .targetPrice(22650.0)
                    .confidenceScore(0.85)
                    .lotSize(50)
                    .investmentRequired(1125000.0)
                    .generatedAt(LocalDateTime.now())
                    .build());

            // Seed BANKNIFTY Option
            tradeSignalRepository.save(TradeSignal.builder()
                    .stock(banknifty)
                    .signalType(SignalType.CALL)
                    .patternName("Neural Pattern CE")
                    .entryPrice(48500.0)
                    .stopLoss(48300.0)
                    .targetPrice(48900.0)
                    .confidenceScore(0.89)
                    .lotSize(15)
                    .investmentRequired(727500.0)
                    .generatedAt(LocalDateTime.now())
                    .build());

            // Seed EICHERMOT Option
            Stock eicherOpt = stockRepository.save(Stock.builder().symbol("EICHERMOT 4500 CE").name("EICHERMOT 4500 CE").sector("Equity").build());
            tradeSignalRepository.save(TradeSignal.builder()
                    .stock(eicherOpt)
                    .signalType(SignalType.CALL)
                    .patternName("AI Momentum Trend")
                    .entryPrice(4520.0)
                    .stopLoss(4480.0)
                    .targetPrice(4600.0)
                    .confidenceScore(0.92)
                    .lotSize(1)
                    .investmentRequired(4520.0)
                    .generatedAt(LocalDateTime.now())
                    .build());

            // Seed TCS Option
            Stock tcsOpt = stockRepository.save(Stock.builder().symbol("TCS 3900 PE").name("TCS 3900 PE").sector("Equity").build());
            tradeSignalRepository.save(TradeSignal.builder()
                    .stock(tcsOpt)
                    .signalType(SignalType.PUT)
                    .patternName("Neural V-Pulse")
                    .entryPrice(3850.0)
                    .stopLoss(3880.0)
                    .targetPrice(3780.0)
                    .confidenceScore(0.72)
                    .lotSize(1)
                    .investmentRequired(3850.0)
                    .generatedAt(LocalDateTime.now())
                    .build());

            // Seed equity signal for Intraday Pulse
            tradeSignalRepository.save(TradeSignal.builder()
                    .stock(reliance)
                    .signalType(SignalType.CALL)
                    .patternName("AI Momentum Trend")
                    .entryPrice(2950.0)
                    .stopLoss(2930.0)
                    .targetPrice(3010.0)
                    .confidenceScore(0.88)
                    .lotSize(1)
                    .investmentRequired(2950.0)
                    .generatedAt(LocalDateTime.now())
                    .build());

            tradeSignalRepository.save(TradeSignal.builder()
                    .stock(tcs)
                    .signalType(SignalType.PUT)
                    .patternName("Neural V-Pulse")
                    .entryPrice(3850.0)
                    .stopLoss(3880.0)
                    .targetPrice(3780.0)
                    .confidenceScore(0.72)
                    .lotSize(1)
                    .investmentRequired(3850.0)
                    .generatedAt(LocalDateTime.now())
                    .build());

            log.info("✅ Database seeded with Equity & Index signals. Dashboard is now ready!");
        }
    }
}


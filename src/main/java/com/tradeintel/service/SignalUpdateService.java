package com.tradeintel.service;

import com.tradeintel.entity.TradeSignal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SignalUpdateService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        
        return emitter;
    }

    public void broadcastSignal(TradeSignal signal) {
// log.info("📢 Broadcasting signal to UI: {}", signal.getStock().getSymbol());
        sendToAll("trade-signal", signal);
    }

    public void broadcastPriceTick(String symbol, Double price) {
        java.util.Map<String, Object> data = java.util.Map.of(
            "symbol", symbol, 
            "price", price, 
            "timestamp", System.currentTimeMillis()
        );
        sendToAll("price-tick", data);
    }

    private void sendToAll(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}

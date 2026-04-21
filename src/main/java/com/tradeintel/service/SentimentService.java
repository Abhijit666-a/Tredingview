package com.tradeintel.service;

import com.tradeintel.dto.SentimentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.ai.chat.client.ChatClient; // COMMENT THIS
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentService {

    // private final ChatClient.Builder chatClientBuilder; // COMMENT THIS
    private final ObjectMapper objectMapper;

    public SentimentResponse analyzeSentiment(String headline) {
        log.info("📰 Analyzing sentiment for headline: {}", headline);

        // Aapan direct Fallback logic vapru ya jo paryant dependency set hot nahi
        log.warn("⚠️ Using Local Sentiment Engine for: {}", headline);
        return getLocalSentiment(headline);
    }

    private SentimentResponse getLocalSentiment(String headline) {
        double score = 0.0;
        String summary = "Neutral news flow.";
        String lowerHeadline = headline.toLowerCase();

        if (lowerHeadline.contains("milestone") || lowerHeadline.contains("partnership") || 
            lowerHeadline.contains("profit") || lowerHeadline.contains("expansion")) {
            score = 0.85;
            summary = "Highly Bullish: Positive corporate developments detected.";
        } else if (lowerHeadline.contains("scrutiny") || lowerHeadline.contains("tax") || 
                   lowerHeadline.contains("pressure") || lowerHeadline.contains("debt") || 
                   lowerHeadline.contains("fines")) {
            score = -0.75;
            summary = "Bearish: Negative regulatory or financial pressure detected.";
        } else if (lowerHeadline.contains("strategic") || lowerHeadline.contains("announces") || 
                   lowerHeadline.contains("upgrade") || lowerHeadline.contains("buyback")) {
            score = 0.50;
            summary = "Bullish Outlook: Growth-oriented action expected.";
        }

        return SentimentResponse.builder()
                .sentimentScore(score)
                .summary(summary)
                .build();
    }
}
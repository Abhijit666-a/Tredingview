package com.tradeintel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AI sentiment analysis response.
 * Expected JSON format from Gemini:
 * {
 *   "sentimentScore": 0.75,
 *   "summary": "Positive outlook due to strong quarterly earnings."
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentResponse {

    @JsonProperty("sentimentScore")
    private Double sentimentScore;

    @JsonProperty("summary")
    private String summary;
}

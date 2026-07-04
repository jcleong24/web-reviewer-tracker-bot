package com.modefair.webreviewer.dto;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * The analysis payload Claude is asked to produce, used as the schema for
 * structured output. It mirrors {@link AssessmentResponse} minus the fields the
 * server already knows ({@code url}, {@code pageTitle}); the service merges those
 * back in when building the client-facing response.
 *
 * @param overallScore       0-100 overall market-potential score
 * @param rating             one of Strong | Promising | Mixed | Weak
 * @param verdict            one candid sentence
 * @param summary            2-4 sentences expanding on the verdict
 * @param dimensions         per-dimension scores and reasoning
 * @param strengths          what is genuinely working in the product's favor
 * @param redFlags           risks or gaps a scout should worry about
 * @param diligenceQuestions pointed questions to ask the founder
 */
@JsonClassDescription("A candid market-potential assessment of a product web page.")
public record ModelAssessment(
        @JsonPropertyDescription("Overall market-potential score from 0 to 100.")
        int overallScore,
        @JsonPropertyDescription("Overall rating: exactly one of Strong, Promising, Mixed, or Weak.")
        String rating,
        @JsonPropertyDescription("One candid sentence summarizing the verdict.")
        String verdict,
        @JsonPropertyDescription("Two to four sentences expanding on the verdict.")
        String summary,
        @JsonPropertyDescription("Per-dimension scores and reasoning.")
        List<DimensionScore> dimensions,
        @JsonPropertyDescription("What is genuinely working in the product's favor.")
        List<String> strengths,
        @JsonPropertyDescription("Risks or gaps a scout should worry about.")
        List<String> redFlags,
        @JsonPropertyDescription("Pointed questions to ask the founder.")
        List<String> diligenceQuestions
) {
}

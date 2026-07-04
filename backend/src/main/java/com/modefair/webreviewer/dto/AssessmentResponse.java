package com.modefair.webreviewer.dto;

import java.util.List;

/**
 * The market-potential assessment returned by {@code POST /api/v1/analyze}.
 * This is the product contract — keep it in sync with the frontend
 * {@code Assessment} type and SPEC.md.
 *
 * @param url                the analyzed URL
 * @param pageTitle          the target page's title
 * @param overallScore       0-100
 * @param rating             one of Strong | Promising | Mixed | Weak
 * @param verdict            one candid sentence
 * @param summary            2-4 sentences expanding on the verdict
 * @param dimensions         per-dimension scores and reasoning
 * @param strengths          what is genuinely working in the product's favor
 * @param redFlags           risks or gaps a scout should worry about
 * @param diligenceQuestions pointed questions to ask the founder
 */
public record AssessmentResponse(
        String url,
        String pageTitle,
        int overallScore,
        String rating,
        String verdict,
        String summary,
        List<DimensionScore> dimensions,
        List<String> strengths,
        List<String> redFlags,
        List<String> diligenceQuestions
) {
}

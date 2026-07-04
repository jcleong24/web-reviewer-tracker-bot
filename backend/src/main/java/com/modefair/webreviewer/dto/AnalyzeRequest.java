package com.modefair.webreviewer.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for {@code POST /api/v1/analyze}.
 *
 * @param url the target product/startup URL to assess
 */
public record AnalyzeRequest(@NotBlank String url) {
}

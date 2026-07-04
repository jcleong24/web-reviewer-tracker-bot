package com.modefair.webreviewer.dto;

/**
 * A single scored assessment dimension.
 *
 * @param name      dimension name (e.g. "Market demand")
 * @param score     0-100
 * @param reasoning 1-2 sentences citing what on the page drove the score
 */
public record DimensionScore(String name, int score, String reasoning) {
}

package com.modefair.webreviewer.dto;

/**
 * Clean, bounded text extracted from a target page, ready for analysis.
 *
 * @param url   the source URL
 * @param title the page title
 * @param text  extracted body text (script/style/nav/footer stripped, truncated)
 */
public record ExtractedContent(String url, String title, String text) {
}

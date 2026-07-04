package com.modefair.webreviewer.service;

import com.modefair.webreviewer.dto.ExtractedContent;

/**
 * Parses raw HTML into clean, bounded text suitable for analysis.
 */
public interface ContentExtractor {

    /**
     * @param url  the source URL (used as the base URI for parsing)
     * @param html raw HTML fetched from the page
     * @return clean, truncated text plus the page title
     */
    ExtractedContent extract(String url, String html);
}

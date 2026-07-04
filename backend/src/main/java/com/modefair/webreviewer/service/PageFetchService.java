package com.modefair.webreviewer.service;

/**
 * Fetches raw HTML from a target URL server-side (the browser never fetches
 * target domains directly). Implementations apply explicit connect/read timeouts.
 */
public interface PageFetchService {

    /**
     * @param url a validated http/https URL
     * @return the raw HTML body of the page
     */
    String fetchHtml(String url);
}

package com.modefair.webreviewer.dto;

/**
 * Clean, bounded text extracted from a target page, ready for analysis.
 *
 * @param url        the source URL
 * @param title      the page title
 * @param text       extracted body text (script/style/nav/footer stripped, truncated)
 * @param screenshot optional PNG screenshot bytes captured by the rendering
 *                   fallback; {@code null} when the page was fetched as raw HTML
 */
public record ExtractedContent(String url, String title, String text, byte[] screenshot) {

    /**
     * Convenience constructor for the raw-HTML path, which has no screenshot.
     */
    public ExtractedContent(String url, String title, String text) {
        this(url, title, text, null);
    }

    /**
     * @param screenshot PNG bytes to attach
     * @return a copy of this content carrying the given screenshot
     */
    public ExtractedContent withScreenshot(byte[] screenshot) {
        return new ExtractedContent(url, title, text, screenshot);
    }
}

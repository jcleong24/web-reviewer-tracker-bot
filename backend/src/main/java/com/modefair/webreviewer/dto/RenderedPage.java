package com.modefair.webreviewer.dto;

/**
 * Output of a headless-browser render: the fully rendered HTML plus a PNG
 * screenshot of the page.
 *
 * @param html       the DOM serialized after JavaScript has run
 * @param screenshot PNG screenshot bytes
 */
public record RenderedPage(String html, byte[] screenshot) {
}

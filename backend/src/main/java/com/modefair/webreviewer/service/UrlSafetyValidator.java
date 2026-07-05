package com.modefair.webreviewer.service;

/**
 * Guards against server-side request forgery (SSRF). Since the backend fetches
 * and renders client-supplied URLs, every target must be verified to resolve to
 * a public address before any outbound request is made.
 */
public interface UrlSafetyValidator {

    /**
     * @param url the target URL
     * @throws IllegalArgumentException if the URL is malformed or resolves to a
     *                                  private, loopback, link-local, or otherwise
     *                                  non-public address
     */
    void verifyPublic(String url);
}

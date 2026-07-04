package com.modefair.webreviewer.service.impl;

import com.modefair.webreviewer.exception.PageFetchException;
import com.modefair.webreviewer.service.PageFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Fetches raw HTML using Spring's {@link RestClient} with explicit connect/read
 * timeouts. Parsing/extraction is handled separately (see the extractor).
 */
@Service
public class RestClientPageFetchService implements PageFetchService {

    private static final Logger log = LoggerFactory.getLogger(RestClientPageFetchService.class);

    private final RestClient restClient;

    public RestClientPageFetchService(@Value("${webreviewer.fetch.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "web-reviewer-bot/0.1")
                .build();
    }

    @Override
    public String fetchHtml(String url) {
        log.info("Fetching target URL: {}", url);
        try {
            String body = restClient.get().uri(url).retrieve().body(String.class);
            if (body == null || body.isBlank()) {
                throw new PageFetchException("The page returned an empty response: " + url, null);
            }
            return body;
        } catch (PageFetchException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to fetch {}: {}", url, e.getMessage());
            throw new PageFetchException("Unable to fetch the page: " + e.getMessage(), e);
        }
    }
}

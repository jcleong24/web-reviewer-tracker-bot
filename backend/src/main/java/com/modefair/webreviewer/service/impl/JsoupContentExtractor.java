package com.modefair.webreviewer.service.impl;

import com.modefair.webreviewer.dto.ExtractedContent;
import com.modefair.webreviewer.exception.PageFetchException;
import com.modefair.webreviewer.service.ContentExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Extracts clean text from HTML using jsoup: strips script/style/nav/footer
 * nodes, keeps headings and body text, and truncates to a bounded length
 * before the content is sent to Claude.
 */
@Service
public class JsoupContentExtractor implements ContentExtractor {

    private static final Logger log = LoggerFactory.getLogger(JsoupContentExtractor.class);

    private final int maxLength;

    public JsoupContentExtractor(@Value("${webreviewer.extract.max-length}") int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public ExtractedContent extract(String url, String html) {
        Document doc = Jsoup.parse(html, url);
        String title = doc.title();

        doc.select("script, style, nav, footer, noscript").remove();
        String text = doc.body() != null ? doc.body().text() : "";

        if (text.length() > maxLength) {
            text = text.substring(0, maxLength);
        }
        if (text.isBlank()) {
            throw new PageFetchException("The page has too little extractable text to assess.", null);
        }

        log.info("Extracted {} chars from '{}'", text.length(), title);
        return new ExtractedContent(url, title, text);
    }
}

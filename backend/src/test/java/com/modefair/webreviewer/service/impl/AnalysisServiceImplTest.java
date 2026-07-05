package com.modefair.webreviewer.service.impl;

import com.anthropic.client.AnthropicClient;
import com.modefair.webreviewer.dto.ExtractedContent;
import com.modefair.webreviewer.dto.RenderedPage;
import com.modefair.webreviewer.exception.PageFetchException;
import com.modefair.webreviewer.service.ContentExtractor;
import com.modefair.webreviewer.service.PageFetchService;
import com.modefair.webreviewer.service.UrlSafetyValidator;
import com.modefair.webreviewer.service.render.PageRenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the thin-page rendering fallback ({@link AnalysisServiceImpl#renderIfThin}).
 * Exercises the branch logic in isolation with mocked collaborators — no browser,
 * no Spring context, no Anthropic call.
 */
@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    private static final int THIN_THRESHOLD = 500;
    private static final String URL = "https://example.com";

    @Mock private PageFetchService pageFetchService;
    @Mock private ContentExtractor contentExtractor;
    @Mock private UrlSafetyValidator urlSafetyValidator;
    @Mock private PageRenderService pageRenderService;
    @Mock private AnthropicClient anthropicClient;

    private AnalysisServiceImpl serviceWith(Optional<PageRenderService> renderer) {
        return new AnalysisServiceImpl(
                pageFetchService, contentExtractor, urlSafetyValidator,
                renderer, anthropicClient, "claude-opus-4-8", THIN_THRESHOLD);
    }

    @Test
    void keepsContentAndSkipsRenderWhenTextMeetsThreshold() {
        AnalysisServiceImpl service = serviceWith(Optional.of(pageRenderService));
        ExtractedContent rich =
                new ExtractedContent(URL, "Title", "a".repeat(THIN_THRESHOLD));

        ExtractedContent result = service.renderIfThin(URL, rich);

        assertSame(rich, result, "content at/above threshold should pass through unchanged");
        verifyNoInteractions(pageRenderService);
    }

    @Test
    void rendersAndAttachesScreenshotWhenTextIsThin() {
        AnalysisServiceImpl service = serviceWith(Optional.of(pageRenderService));
        ExtractedContent thin = new ExtractedContent(URL, "Title", "tiny");
        byte[] screenshot = {1, 2, 3};
        when(pageRenderService.render(URL))
                .thenReturn(new RenderedPage("<html>rich</html>", screenshot));
        when(contentExtractor.extract(URL, "<html>rich</html>"))
                .thenReturn(new ExtractedContent(URL, "Title", "rich rendered text"));

        ExtractedContent result = service.renderIfThin(URL, thin);

        assertEquals("rich rendered text", result.text());
        assertArrayEquals(screenshot, result.screenshot());
    }

    @Test
    void degradesToThinTextWhenRenderFails() {
        AnalysisServiceImpl service = serviceWith(Optional.of(pageRenderService));
        ExtractedContent thin = new ExtractedContent(URL, "Title", "tiny");
        when(pageRenderService.render(URL))
                .thenThrow(new PageFetchException("render boom", null));

        ExtractedContent result = service.renderIfThin(URL, thin);

        assertSame(thin, result, "a render failure should fall back to the original thin content");
        assertNull(result.screenshot());
    }

    @Test
    void keepsThinTextWhenRendererDisabled() {
        AnalysisServiceImpl service = serviceWith(Optional.empty());
        ExtractedContent thin = new ExtractedContent(URL, "Title", "tiny");

        ExtractedContent result = service.renderIfThin(URL, thin);

        assertSame(thin, result, "with no renderer bean, thin content passes through unchanged");
        verifyNoInteractions(pageRenderService);
    }
}

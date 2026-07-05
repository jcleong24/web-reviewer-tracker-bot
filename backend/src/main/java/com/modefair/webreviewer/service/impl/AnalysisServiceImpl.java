package com.modefair.webreviewer.service.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.modefair.webreviewer.dto.AssessmentResponse;
import com.modefair.webreviewer.dto.ExtractedContent;
import com.modefair.webreviewer.dto.ModelAssessment;
import com.modefair.webreviewer.dto.RenderedPage;
import com.modefair.webreviewer.service.AnalysisService;
import com.modefair.webreviewer.service.ContentExtractor;
import com.modefair.webreviewer.service.PageFetchService;
import com.modefair.webreviewer.service.UrlSafetyValidator;
import com.modefair.webreviewer.service.render.PageRenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Default {@link AnalysisService} implementation: validates the URL, fetches and
 * extracts the page, then asks Claude to score its market potential.
 *
 * <p>The analyze step calls Claude through the official Anthropic Java SDK using the
 * configured model with adaptive extended thinking and schema-constrained output,
 * so the response deserializes directly into {@link ModelAssessment}.
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    private static final long MAX_TOKENS = 16_000L;

    private static final String SYSTEM_PROMPT = """
            You are a seasoned market analyst assessing the market potential of a product
            from its web page. Be candid and evidence-based: ground every score and claim in
            what the page actually says, and do not invent facts that are not present.

            Produce an overall score (0-100) and a rating of exactly one of:
            Strong, Promising, Mixed, or Weak.

            Score these eight dimensions (each 0-100), using these exact names:
            - Market demand
            - Willingness to try
            - Willingness to pay
            - Differentiation
            - Target audience clarity
            - Value proposition clarity
            - Traction & credibility signals
            - Success likelihood

            For each dimension give 1-2 sentences of reasoning citing what on the page drove
            the score. Also list genuine strengths, red flags a scout should worry about, and
            pointed diligence questions to ask the founder. If the page is thin on information,
            say so and score conservatively.""";

    private final PageFetchService pageFetchService;
    private final ContentExtractor contentExtractor;
    private final UrlSafetyValidator urlSafetyValidator;
    private final Optional<PageRenderService> pageRenderService;
    private final AnthropicClient anthropicClient;
    private final String anthropicModel;
    private final int thinThreshold;

    public AnalysisServiceImpl(PageFetchService pageFetchService,
                               ContentExtractor contentExtractor,
                               UrlSafetyValidator urlSafetyValidator,
                               Optional<PageRenderService> pageRenderService,
                               AnthropicClient anthropicClient,
                               @Value("${anthropic.model}") String anthropicModel,
                               @Value("${webreviewer.fetch.thin-threshold}") int thinThreshold) {
        this.pageFetchService = pageFetchService;
        this.contentExtractor = contentExtractor;
        this.urlSafetyValidator = urlSafetyValidator;
        this.pageRenderService = pageRenderService;
        this.anthropicClient = anthropicClient;
        this.anthropicModel = anthropicModel;
        this.thinThreshold = thinThreshold;
    }

    @Override
    public AssessmentResponse analyze(String url) {
        if (!isValidHttpUrl(url)) {
            throw new IllegalArgumentException("A valid http/https URL is required.");
        }
        urlSafetyValidator.verifyPublic(url);

        String html = pageFetchService.fetchHtml(url);
        ExtractedContent content = contentExtractor.extract(url, html);
        content = renderIfThin(url, content);

        log.info("Analyzing '{}' with model {}", content.title(), anthropicModel);
        ModelAssessment assessment = requestAssessment(content);

        return new AssessmentResponse(
                content.url(),
                content.title(),
                assessment.overallScore(),
                assessment.rating(),
                assessment.verdict(),
                assessment.summary(),
                assessment.dimensions(),
                assessment.strengths(),
                assessment.redFlags(),
                assessment.diligenceQuestions()
        );
    }

    /**
     * If the raw fetch produced too little text, re-fetch with the headless
     * browser (when enabled) and attach a screenshot. On any rendering failure
     * we keep the original thin content rather than failing the request.
     *
     * <p>Package-private so the fallback branching can be unit-tested directly.
     */
    ExtractedContent renderIfThin(String url, ExtractedContent content) {
        if (content.text().length() >= thinThreshold || pageRenderService.isEmpty()) {
            return content;
        }
        log.info("Extracted text is thin ({} chars); falling back to Playwright for {}",
                content.text().length(), url);
        try {
            RenderedPage rendered = pageRenderService.get().render(url);
            ExtractedContent reExtracted = contentExtractor.extract(url, rendered.html());
            return reExtracted.withScreenshot(rendered.screenshot());
        } catch (RuntimeException e) {
            log.warn("Playwright fallback failed for {}; using thin text. Cause: {}",
                    url, e.getMessage());
            return content;
        }
    }

    private ModelAssessment requestAssessment(ExtractedContent content) {
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(anthropicModel)
                .maxTokens(MAX_TOKENS)
                .thinking(ThinkingConfigAdaptive.builder().build())
                .system(SYSTEM_PROMPT);

        if (content.screenshot() != null && content.screenshot().length > 0) {
            builder.addUserMessageOfBlockParams(buildMultimodalPrompt(content));
        } else {
            builder.addUserMessage(buildUserPrompt(content));
        }

        StructuredMessageCreateParams<ModelAssessment> params =
                builder.outputConfig(ModelAssessment.class).build();

        return anthropicClient.messages().create(params).content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Claude returned no structured assessment for " + content.url()));
    }

    private static List<ContentBlockParam> buildMultimodalPrompt(ExtractedContent content) {
        String base64 = Base64.getEncoder().encodeToString(content.screenshot());
        ImageBlockParam screenshot = ImageBlockParam.builder()
                .source(Base64ImageSource.builder()
                        .mediaType(Base64ImageSource.MediaType.IMAGE_PNG)
                        .data(base64)
                        .build())
                .build();
        return List.of(
                ContentBlockParam.ofText(TextBlockParam.builder()
                        .text(buildUserPrompt(content))
                        .build()),
                ContentBlockParam.ofImage(screenshot));
    }

    private static String buildUserPrompt(ExtractedContent content) {
        return """
                Assess the market potential of this web page.

                URL: %s
                Title: %s

                Page content:
                %s""".formatted(content.url(), content.title(), content.text());
    }

    private static boolean isValidHttpUrl(String url) {
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (RuntimeException e) {
            return false;
        }
    }
}

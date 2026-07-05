package com.modefair.webreviewer.service.render;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import com.modefair.webreviewer.dto.RenderedPage;
import com.modefair.webreviewer.exception.PageFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Renders pages with a headless Chromium instance via Playwright, executing
 * JavaScript so client-rendered content becomes visible, then captures a
 * screenshot. Only instantiated when {@code webreviewer.fetch.playwright.enabled}
 * is true, so the browser binaries are never launched otherwise.
 *
 * <p>A single browser is launched for the bean's lifetime; {@link #render} is
 * synchronized because a Playwright instance is not thread-safe. This suits the
 * low-frequency fallback path where it is invoked.
 */
@Service
@ConditionalOnProperty(name = "webreviewer.fetch.playwright.enabled", havingValue = "true")
public class PlaywrightPageRenderService implements PageRenderService, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightPageRenderService.class);

    private final double timeoutMs;
    private final boolean fullPage;
    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightPageRenderService(
            @Value("${webreviewer.fetch.playwright.timeout-ms}") double timeoutMs,
            @Value("${webreviewer.fetch.playwright.full-page}") boolean fullPage) {
        this.timeoutMs = timeoutMs;
        this.fullPage = fullPage;
        log.info("Launching headless Chromium for the rendering fallback");
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
    }

    @Override
    public synchronized RenderedPage render(String url) {
        log.info("Rendering target URL with Playwright: {}", url);
        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setUserAgent("web-reviewer-bot/0.1"))) {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(timeoutMs));
            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions().setFullPage(fullPage));
            return new RenderedPage(page.content(), screenshot);
        } catch (RuntimeException e) {
            throw new PageFetchException("Unable to render the page: " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}

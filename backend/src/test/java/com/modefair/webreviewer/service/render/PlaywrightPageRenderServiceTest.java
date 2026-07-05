package com.modefair.webreviewer.service.render;

import com.modefair.webreviewer.dto.RenderedPage;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Manual smoke test for the Playwright rendering fallback. Requires the Chromium
 * binaries to be installed and outbound network access, so it is not part of the
 * normal unit suite — run it explicitly:
 *
 * <pre>mvn -Dtest=PlaywrightPageRenderServiceTest test</pre>
 *
 * It launches a real browser, renders a stable page, asserts that HTML and a
 * screenshot came back, and writes the PNG to target/ so you can eyeball it.
 */
class PlaywrightPageRenderServiceTest {

    @Test
    void rendersPageAndCapturesScreenshot() throws Exception {
        PlaywrightPageRenderService service =
                new PlaywrightPageRenderService(15_000, false);
        try {
            RenderedPage page = service.render("https://example.com");

            assertTrue(page.html().contains("Example Domain"),
                    "rendered HTML should contain the page's visible text");
            assertTrue(page.screenshot().length > 0,
                    "a non-empty screenshot should be captured");

            Path out = Path.of("target", "playwright-smoke.png");
            Files.write(out, page.screenshot());
            System.out.println("Screenshot written to " + out.toAbsolutePath()
                    + " (" + page.screenshot().length + " bytes)");
        } finally {
            service.destroy();
        }
    }
}

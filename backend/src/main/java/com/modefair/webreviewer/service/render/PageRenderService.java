package com.modefair.webreviewer.service.render;

import com.modefair.webreviewer.dto.RenderedPage;

/**
 * Renders a page in a real headless browser (executing JavaScript) and captures
 * a screenshot. Used as a fallback when the raw
 * {@link com.modefair.webreviewer.service.PageFetchService} yields too little
 * text to assess — typically for client-rendered single-page apps.
 */
public interface PageRenderService {

    /**
     * @param url a validated, public http/https URL
     * @return the rendered HTML plus a screenshot
     */
    RenderedPage render(String url);
}

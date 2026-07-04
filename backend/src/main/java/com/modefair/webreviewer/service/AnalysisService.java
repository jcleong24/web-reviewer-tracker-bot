package com.modefair.webreviewer.service;

import com.modefair.webreviewer.dto.AssessmentResponse;

/**
 * Orchestrates the fetch -&gt; extract -&gt; analyze pipeline for a target URL and
 * returns a validated market-potential assessment.
 */
public interface AnalysisService {

    /**
     * @param url a http/https URL to assess
     * @return the market-potential assessment
     */
    AssessmentResponse analyze(String url);
}

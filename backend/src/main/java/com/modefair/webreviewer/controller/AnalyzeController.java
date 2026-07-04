package com.modefair.webreviewer.controller;

import com.modefair.webreviewer.dto.AnalyzeRequest;
import com.modefair.webreviewer.dto.AssessmentResponse;
import com.modefair.webreviewer.service.AnalysisService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for market-potential analysis.
 */
@RestController
@RequestMapping("/api/v1")
public class AnalyzeController {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeController.class);

    private final AnalysisService analysisService;

    public AnalyzeController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AssessmentResponse> analyze(@Valid @RequestBody AnalyzeRequest request) {
        log.info("Received analyze request for {}", request.url());
        return ResponseEntity.ok(analysisService.analyze(request.url()));
    }
}

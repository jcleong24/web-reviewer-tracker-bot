package com.modefair.webreviewer.exception;

import com.anthropic.errors.AnthropicServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps pipeline and validation errors to clean HTTP responses for the UI layer.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Invalid input (bad/missing URL, wrong scheme) -> 400. */
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleBadInput(Exception ex) {
        String message = (ex instanceof MethodArgumentNotValidException)
                ? "Invalid request: a valid 'url' is required."
                : ex.getMessage();
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    /** Upstream page could not be fetched or read -> 502. */
    @ExceptionHandler(PageFetchException.class)
    public ResponseEntity<Map<String, String>> handleFetch(PageFetchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", ex.getMessage()));
    }

    /** Claude/Anthropic SDK failure -> 502 (details logged, not leaked to the client). */
    @ExceptionHandler(AnthropicServiceException.class)
    public ResponseEntity<Map<String, String>> handleAnthropic(AnthropicServiceException ex) {
        log.error("Anthropic API error while analyzing", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "The analysis service is temporarily unavailable. Please try again."));
    }

    /** Anything unexpected -> 500 (details logged, not leaked to the client). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Unexpected error handling analyze request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error."));
    }
}

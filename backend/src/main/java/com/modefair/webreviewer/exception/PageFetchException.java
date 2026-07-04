package com.modefair.webreviewer.exception;

/**
 * Raised when a target URL cannot be fetched or yields no usable content
 * (unreachable host, timeout, non-HTML response, or too little text to assess).
 */
public class PageFetchException extends RuntimeException {

    public PageFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}

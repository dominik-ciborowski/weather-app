package com.weather.backend.exception;

public class UpstreamUnavailableException extends RuntimeException {

    private final String url;

    public UpstreamUnavailableException(String message, String url, Throwable cause) {
        super(message, cause);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

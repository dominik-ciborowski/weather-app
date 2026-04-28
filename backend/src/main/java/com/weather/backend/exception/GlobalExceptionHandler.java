package com.weather.backend.exception;

import com.weather.backend.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCityNotFound(CityNotFoundException exception) {
        log.warn("City lookup failed: {}", exception.getMessage());
        return build(HttpStatus.NOT_FOUND, "City not found", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("Invalid request parameter: {}", exception.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter");
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(Exception exception) {
        log.warn("Request validation failed: {}", exception.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Request validation failed");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Illegal request argument: {}", exception.getMessage());
        return build(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamApi(WeatherApiException exception) {
        HttpStatus status = mapStatus(exception);
        log.error(
            "Weather API failure. upstreamStatus={}, downstreamStatus={}, message={}, url={}",
            exception.getWeatherApiStatus().value(),
            status.value(),
            exception.getMessage(),
            exception.getUrl()
        );
        return build(status, exception.getMessage());
    }

    @ExceptionHandler(UpstreamUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamUnavailable(UpstreamUnavailableException exception) {
        log.error("Weather API unavailable. message={}, url={}", exception.getMessage(), exception.getUrl(), exception);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        log.error("Unexpected server error", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return build(status, status.getReasonPhrase(), message);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        ErrorResponse body = new ErrorResponse(
            status.value(),
            error,
            message,
            Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus mapStatus(WeatherApiException exception) {
        int statusCode = exception.getWeatherApiStatus().value();
        if (statusCode == 429) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (statusCode == 401 || statusCode >= 500) {
            return HttpStatus.BAD_GATEWAY;
        }
        return HttpStatus.BAD_GATEWAY;
    }
}

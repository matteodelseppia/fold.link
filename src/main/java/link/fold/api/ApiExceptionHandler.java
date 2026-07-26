package link.fold.api;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import link.fold.domain.AliasNotFoundException;
import link.fold.domain.InvalidDestinationException;
import link.fold.domain.StorageUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates domain and framework exceptions into the single stable error contract from ADR-001:
 * always {@code {error, message}}, never a parser/framework stack trace or internal storage detail.
 *
 * <p>The two 404 cases (a missing/invalid alias, or any other unmapped path) are the exception:
 * browsers navigating there directly (Accept including {@code text/html}) get the friendly static
 * page ({@code static/404.html}) instead of a bare JSON body, while API clients keep the stable
 * {@code {error, message}} contract.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  private static final String NOT_FOUND_PAGE = loadNotFoundPage();

  private static String loadNotFoundPage() {
    try {
      byte[] bytes =
          FileCopyUtils.copyToByteArray(new ClassPathResource("static/404.html").getInputStream());
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to load static/404.html", e);
    }
  }

  private static boolean wantsHtml(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
  }

  @ExceptionHandler(InvalidDestinationException.class)
  public ResponseEntity<ErrorResponse> handleInvalidDestination(InvalidDestinationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
            .orElse("Invalid request");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("VALIDATION_ERROR", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("VALIDATION_ERROR", "Request body is missing or malformed"));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException e) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(new ErrorResponse("UNSUPPORTED_MEDIA_TYPE", "Content-Type must be application/json"));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> handleNoResourceFound(
      NoResourceFoundException e, HttpServletRequest request) {
    // Requests that match no controller mapping and no real static asset (e.g. an unmapped
    // /api/** path) fall through to Spring's static-resource handler, which raises this instead
    // of a plain 404 - map it back to one so it isn't swallowed by the generic 500 fallback.
    if (wantsHtml(request)) {
      return notFoundHtmlResponse();
    }
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(AliasNotFoundException.class)
  public ResponseEntity<?> handleAliasNotFound(
      AliasNotFoundException e, HttpServletRequest request) {
    if (wantsHtml(request)) {
      return notFoundHtmlResponse();
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("ALIAS_NOT_FOUND", "No link exists for this alias"));
  }

  private static ResponseEntity<String> notFoundHtmlResponse() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.TEXT_HTML)
        .body(NOT_FOUND_PAGE);
  }

  @ExceptionHandler(StorageUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleStorageUnavailable(StorageUnavailableException e) {
    log.warn("Storage unavailable: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse("STORAGE_ERROR", "The service is temporarily unavailable"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    log.error("Unexpected error handling request", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
  }
}

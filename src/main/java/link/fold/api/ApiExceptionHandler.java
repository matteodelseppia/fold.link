package link.fold.api;

import link.fold.domain.AliasNotFoundException;
import link.fold.domain.InvalidDestinationException;
import link.fold.domain.StorageUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates domain and framework exceptions into the single stable error contract from ADR-001:
 * always {@code {error, message}}, never a parser/framework stack trace or internal storage detail.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

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
  public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
    // Requests that match no controller mapping and no real static asset (e.g. an unmapped
    // /api/** path) fall through to Spring's static-resource handler, which raises this instead
    // of a plain 404 - map it back to one so it isn't swallowed by the generic 500 fallback.
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(AliasNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAliasNotFound(AliasNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("ALIAS_NOT_FOUND", "No link exists for this alias"));
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

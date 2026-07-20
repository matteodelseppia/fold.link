package link.fold.api;

/** Stable error contract shared by every failure response (ADR-001): a safe code and message. */
public record ErrorResponse(String error, String message) {}

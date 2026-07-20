package link.fold.api;

/** Success response body for {@code POST /api/v1/links} (ADR-001). */
public record LinkCreationResponse(String alias, String shortUrl, String destination) {}

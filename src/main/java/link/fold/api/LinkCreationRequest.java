package link.fold.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for {@code POST /api/v1/links} (ADR-001). */
public record LinkCreationRequest(@NotBlank @Size(max = 2048) String url) {}

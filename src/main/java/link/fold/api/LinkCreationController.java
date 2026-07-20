package link.fold.api;

import jakarta.validation.Valid;
import link.fold.config.AppProperties;
import link.fold.domain.LinkCreationService;
import link.fold.domain.LinkMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@code POST /api/v1/links} (ADR-001). The absolute short URL is always built from the
 * configured {@code app.base-url}, never from request headers - {@code Host}/{@code
 * X-Forwarded-Host} cannot spoof the public origin returned to a caller.
 */
@RestController
@RequestMapping("/api/v1/links")
public class LinkCreationController {

  private final LinkCreationService linkCreationService;
  private final String publicOrigin;

  public LinkCreationController(LinkCreationService linkCreationService, AppProperties properties) {
    this.linkCreationService = linkCreationService;
    String baseUrl = properties.baseUrl();
    this.publicOrigin =
        baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LinkCreationResponse> create(
      @Valid @RequestBody LinkCreationRequest request) {
    LinkMapping mapping = linkCreationService.createLink(request.url());
    String shortUrl = publicOrigin + "/" + mapping.alias();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new LinkCreationResponse(mapping.alias(), shortUrl, mapping.destination()));
  }
}

package link.fold.api;

import java.net.URI;
import link.fold.domain.LinkClickService;
import link.fold.domain.LinkLookupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@code GET /{alias}} (ADR-001) - the public short URL itself, so it is intentionally
 * unversioned and lives at the path root. The path variable pattern only matches the alias alphabet
 * (letters, digits, {@code -}/{@code _}), so it never captures {@code /api/**} requests or dotted
 * static-asset paths (e.g. {@code /favicon.ico}); those are matched by more specific
 * mappings/resource handlers first regardless; the pattern is an extra safeguard against
 * accidentally treating them as candidate aliases.
 */
@RestController
public class RedirectController {

  private final LinkLookupService linkLookupService;
  private final LinkClickService linkClickService;

  public RedirectController(
      LinkLookupService linkLookupService, LinkClickService linkClickService) {
    this.linkLookupService = linkLookupService;
    this.linkClickService = linkClickService;
  }

  @GetMapping("/{alias:[A-Za-z0-9_-]+}")
  public ResponseEntity<Void> redirect(@PathVariable String alias) {
    String destination = linkLookupService.resolve(alias);
    linkClickService.recordClick(alias);
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(destination)).build();
  }
}

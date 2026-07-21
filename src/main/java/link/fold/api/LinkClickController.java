package link.fold.api;

import link.fold.domain.LinkClickService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@code GET /api/v1/links/{alias}/clicks}: how many times an alias has been redirected
 * through, as a bare number - no wrapper object, no UI page. Aliases that don't match the contract
 * shape are rejected as {@code 404}, the same way {@code GET /{alias}} rejects them.
 */
@RestController
@RequestMapping("/api/v1/links")
public class LinkClickController {

  private final LinkClickService linkClickService;

  public LinkClickController(LinkClickService linkClickService) {
    this.linkClickService = linkClickService;
  }

  @GetMapping(value = "/{alias:[A-Za-z0-9_-]+}/clicks", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Long> clicks(@PathVariable String alias) {
    return ResponseEntity.ok(linkClickService.countClicks(alias));
  }
}

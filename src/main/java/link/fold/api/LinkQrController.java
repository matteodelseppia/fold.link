package link.fold.api;

import java.time.Duration;
import link.fold.config.AppProperties;
import link.fold.domain.LinkLookupService;
import link.fold.domain.QrCodeService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@code GET /api/v1/links/{alias}/qr}: a PNG QR code that encodes the alias's own short
 * URL. The alias must resolve to a live mapping - the same {@link LinkLookupService#resolve}
 * shape/existence check the redirect uses - but resolving it here never counts as a click; only an
 * actual {@code GET /{alias}} redirect does. Nothing is cached server-side: encoding a short,
 * fixed-length URL is cheap enough to redo on every request, so the response instead carries a
 * cache-control header letting the browser avoid refetching an unchanged code.
 */
@RestController
@RequestMapping("/api/v1/links")
public class LinkQrController {

  private static final CacheControl QR_CACHE_CONTROL =
      CacheControl.maxAge(Duration.ofHours(1)).mustRevalidate();

  private final LinkLookupService linkLookupService;
  private final QrCodeService qrCodeService;
  private final String publicOrigin;

  public LinkQrController(
      LinkLookupService linkLookupService, QrCodeService qrCodeService, AppProperties properties) {
    this.linkLookupService = linkLookupService;
    this.qrCodeService = qrCodeService;
    String baseUrl = properties.baseUrl();
    this.publicOrigin =
        baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  @GetMapping(value = "/{alias:[A-Za-z0-9_-]+}/qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> qr(@PathVariable String alias) {
    linkLookupService.resolve(alias);
    String shortUrl = publicOrigin + "/" + alias;
    byte[] png = qrCodeService.generatePng(shortUrl);
    return ResponseEntity.ok().cacheControl(QR_CACHE_CONTROL).body(png);
  }
}

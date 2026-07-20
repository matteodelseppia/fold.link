package link.fold.domain;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Component;

/**
 * Validates that a candidate destination is an absolute {@code http}/{@code https} URL with a
 * syntactically valid host, then canonicalizes it for storage.
 *
 * <p>Canonicalization only lower-cases the scheme and host; the raw (still percent-encoded) path,
 * query, and fragment are copied through unchanged so resource semantics never change (ADR-001).
 */
@Component
public class DestinationValidator {

  private static final String HTTP = "http";
  private static final String HTTPS = "https";

  public String validateAndCanonicalize(String rawUrl) {
    if (rawUrl == null || rawUrl.isBlank()) {
      throw new InvalidDestinationException("url must not be blank");
    }

    URI uri;
    try {
      uri = new URI(rawUrl.trim());
    } catch (URISyntaxException e) {
      throw new InvalidDestinationException("url is not a well-formed URI");
    }

    String scheme = uri.getScheme();
    if (scheme == null || !(HTTP.equalsIgnoreCase(scheme) || HTTPS.equalsIgnoreCase(scheme))) {
      throw new InvalidDestinationException("url must use the http or https scheme");
    }

    if (uri.getRawUserInfo() != null) {
      throw new InvalidDestinationException("url must not contain credentials");
    }

    String host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw new InvalidDestinationException("url must have a valid host");
    }

    StringBuilder canonical = new StringBuilder();
    canonical.append(scheme.toLowerCase()).append("://").append(host.toLowerCase());
    int port = uri.getPort();
    if (port != -1) {
      canonical.append(':').append(port);
    }
    String rawPath = uri.getRawPath();
    if (rawPath != null && !rawPath.isEmpty()) {
      canonical.append(rawPath);
    }
    String rawQuery = uri.getRawQuery();
    if (rawQuery != null) {
      canonical.append('?').append(rawQuery);
    }
    String rawFragment = uri.getRawFragment();
    if (rawFragment != null) {
      canonical.append('#').append(rawFragment);
    }

    return canonical.toString();
  }

  public boolean isValidCanonical(String url) {
    try {
      validateAndCanonicalize(url);
      return true;
    } catch (InvalidDestinationException e) {
      return false;
    }
  }
}

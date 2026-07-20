package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DestinationValidatorTests {

  private final DestinationValidator validator = new DestinationValidator();

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "   ",
        "not a url",
        "ftp://example.com",
        "javascript:alert(1)",
        "file:///etc/passwd",
        "/relative/path",
        "example.com",
        "//example.com/path",
        "http://",
        "http:///path",
        "http://user:pass@example.com",
      })
  void rejectsInvalidOrUnsupportedDestinations(String rawUrl) {
    assertThatThrownBy(() -> validator.validateAndCanonicalize(rawUrl))
        .isInstanceOf(InvalidDestinationException.class);
    assertThat(validator.isValidCanonical(rawUrl)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"http://example.com", "https://example.com", "HTTPS://EXAMPLE.COM"})
  void acceptsHttpAndHttpsSchemes(String rawUrl) {
    assertThat(validator.isValidCanonical(rawUrl)).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "HTTPS://Example.com/Path, https://example.com/Path",
    "http://EXAMPLE.com:8080/x, http://example.com:8080/x",
    "https://example.com/a/b?z=1&a=2, https://example.com/a/b?z=1&a=2",
    "https://example.com/a%20b, https://example.com/a%20b",
    "https://example.com/path#frag, https://example.com/path#frag",
    "https://example.com, https://example.com",
    "https://192.168.0.1/path, https://192.168.0.1/path",
    "https://[::1]:8443/path, https://[::1]:8443/path",
  })
  void canonicalizesSchemeAndHostCasingOnly(String rawUrl, String expectedCanonical) {
    assertThat(validator.validateAndCanonicalize(rawUrl)).isEqualTo(expectedCanonical);
  }

  @org.junit.jupiter.api.Test
  void roundTripsThroughRepeatedCanonicalization() {
    String canonical = validator.validateAndCanonicalize("HTTPS://Example.com/a?b=1#c");
    assertThat(validator.validateAndCanonicalize(canonical)).isEqualTo(canonical);
  }
}

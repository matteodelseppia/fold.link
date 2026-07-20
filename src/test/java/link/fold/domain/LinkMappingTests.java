package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LinkMappingTests {

  @Test
  void constructsWithValidAliasAndDestination() {
    LinkMapping mapping = new LinkMapping("abc12345", "https://example.com/path");

    assertThat(mapping.alias()).isEqualTo("abc12345");
    assertThat(mapping.destination()).isEqualTo("https://example.com/path");
  }

  @Test
  void rejectsNullAlias() {
    assertThatThrownBy(() -> new LinkMapping(null, "https://example.com"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsBlankAlias() {
    assertThatThrownBy(() -> new LinkMapping("   ", "https://example.com"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsNullDestination() {
    assertThatThrownBy(() -> new LinkMapping("abc12345", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsBlankDestination() {
    assertThatThrownBy(() -> new LinkMapping("abc12345", "   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void equalityIsValueBased() {
    LinkMapping a = new LinkMapping("abc12345", "https://example.com");
    LinkMapping b = new LinkMapping("abc12345", "https://example.com");

    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);
  }
}

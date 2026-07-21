package link.fold.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import link.fold.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RedisKeyCodecTests {

  private final AppProperties properties =
      new AppProperties(
          "https://fold.link",
          new AppProperties.Alias(8, 5),
          new AppProperties.Redis("v1:link:", Duration.ofDays(3)));

  private final RedisKeyCodec codec = new RedisKeyCodec(properties);

  @Test
  void encodesAValidAliasWithTheConfiguredPrefix() {
    assertThat(codec.toKey("abcDEF12")).isEqualTo("v1:link:abcDEF12");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "short",
        "waytoolongofanalias",
        "has space",
        "has:colon",
        "v1:link:abcDEF12",
      })
  void rejectsAliasesThatDoNotMatchTheContractShape(String invalidAlias) {
    assertThatThrownBy(() -> codec.toKey(invalidAlias))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsNullAlias() {
    assertThatThrownBy(() -> codec.toKey(null)).isInstanceOf(IllegalArgumentException.class);
  }
}

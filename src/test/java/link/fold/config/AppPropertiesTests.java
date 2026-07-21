package link.fold.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Covers valid, missing-required, and out-of-range binding for {@link AppProperties}, independent
 * of the full application context.
 */
class AppPropertiesTests {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(EnableAppProperties.class);

  @EnableConfigurationProperties(AppProperties.class)
  static class EnableAppProperties {}

  @Test
  void bindsExplicitValidValues() {
    runner
        .withPropertyValues(
            "app.base-url=https://fold.link",
            "app.alias.length=10",
            "app.alias.retry-count=3",
            "app.redis.key-prefix=v2:link:",
            "app.redis.ttl=1d")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              AppProperties props = context.getBean(AppProperties.class);
              assertThat(props.baseUrl()).isEqualTo("https://fold.link");
              assertThat(props.alias().length()).isEqualTo(10);
              assertThat(props.alias().retryCount()).isEqualTo(3);
              assertThat(props.redis().keyPrefix()).isEqualTo("v2:link:");
              assertThat(props.redis().ttl()).isEqualTo(Duration.ofDays(1));
            });
  }

  @Test
  void usesAdrDefaultRetryCountWhenOmitted() {
    // A record only binds as a nested object if at least one property
    // under its prefix is present (app.alias.length here); retry-count
    // is omitted to prove it falls back to the ADR-matching default.
    // The full-context defaults (including the Redis key prefix, whose
    // single field can't be partially omitted the same way) are covered
    // by AppPropertiesDefaultsTests against the real application.yml.
    runner
        .withPropertyValues("app.base-url=https://fold.link", "app.alias.length=8")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              AppProperties props = context.getBean(AppProperties.class);
              assertThat(props.alias().retryCount()).isEqualTo(5);
            });
  }

  @Test
  void failsWhenRedisTtlIsZero() {
    runner
        .withPropertyValues(
            "app.base-url=https://fold.link", "app.redis.key-prefix=v1:link:", "app.redis.ttl=0s")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenRedisTtlIsNegative() {
    runner
        .withPropertyValues(
            "app.base-url=https://fold.link", "app.redis.key-prefix=v1:link:", "app.redis.ttl=-1d")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenBaseUrlMissing() {
    runner.run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenBaseUrlIsNotAWellFormedUrl() {
    runner
        .withPropertyValues("app.base-url=not-a-url")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenAliasLengthTooShort() {
    runner
        .withPropertyValues("app.base-url=https://fold.link", "app.alias.length=3")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenAliasLengthTooLong() {
    runner
        .withPropertyValues("app.base-url=https://fold.link", "app.alias.length=64")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenRetryCountIsNotPositive() {
    runner
        .withPropertyValues("app.base-url=https://fold.link", "app.alias.retry-count=0")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void failsWhenRetryCountTooHigh() {
    runner
        .withPropertyValues("app.base-url=https://fold.link", "app.alias.retry-count=21")
        .run(context -> assertThat(context).hasFailed());
  }
}

package link.fold.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Confirms the real application.yml resolves app.alias.*, app.redis.key-prefix, and app.redis.ttl
 * to their ADR-matching defaults when the corresponding environment variables aren't set, using the
 * local profile (which only supplies REDIS_HOST/APP_BASE_URL defaults, nothing for alias/redis).
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("local")
class AppPropertiesDefaultsTests {

  @Autowired private AppProperties appProperties;

  @Test
  void aliasAndRedisDefaultsMatchAdr() {
    assertThat(appProperties.alias().length()).isEqualTo(8);
    assertThat(appProperties.alias().retryCount()).isEqualTo(5);
    assertThat(appProperties.redis().keyPrefix()).isEqualTo("v1:link:");
    assertThat(appProperties.redis().ttl()).isEqualTo(Duration.ofDays(3));
  }
}

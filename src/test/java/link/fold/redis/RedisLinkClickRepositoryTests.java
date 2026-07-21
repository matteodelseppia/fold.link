package link.fold.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Contract tests for the Redis click-counter adapter, run against a real, reachable Redis (see
 * scripts/dev/redis-start.sh / the CI "redis" service) - skipped, not failed, if none is
 * reachable, matching {@code link.fold.health}'s convention. Each test class run gets its own
 * randomly generated key prefix so parallel/CI runs can never share keys, and every key created is
 * cleaned up afterward.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class RedisLinkClickRepositoryTests {

  private static final String TEST_PREFIX = "test:" + UUID.randomUUID() + ":";

  @DynamicPropertySource
  static void uniqueKeyPrefix(DynamicPropertyRegistry registry) {
    registry.add("app.redis.key-prefix", () -> TEST_PREFIX);
  }

  @BeforeAll
  static void assumeRedisIsReachable() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 6379), 500);
    } catch (IOException e) {
      assumeTrue(
          false,
          "Redis is not reachable on localhost:6379 - skipping (run scripts/dev/redis-start.sh first)");
    }
  }

  @Autowired private RedisLinkClickRepository repository;
  @Autowired private RedisKeyCodec keyCodec;
  @Autowired private StringRedisTemplate redisTemplate;

  @AfterEach
  void cleanUpTestKeys() {
    Set<String> keys = redisTemplate.keys(TEST_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  private String freshAlias() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }

  @Test
  void countClicksIsZeroForAnAliasThatHasNeverBeenClicked() {
    assertThat(repository.countClicks(freshAlias())).isZero();
  }

  @Test
  void recordClickIncrementsTheCounter() {
    String alias = freshAlias();

    repository.recordClick(alias);
    repository.recordClick(alias);
    repository.recordClick(alias);

    assertThat(repository.countClicks(alias)).isEqualTo(3L);
  }

  @Test
  void recordClickSetsAnExpiryOnTheCounterKey() {
    String alias = freshAlias();

    repository.recordClick(alias);

    Long ttl = redisTemplate.getExpire(keyCodec.toClickKey(alias));
    assertThat(ttl).isNotNull().isPositive().isLessThanOrEqualTo(Duration.ofDays(3).toSeconds());
  }

  @Test
  void clicksOnDifferentAliasesAreCountedIndependently() {
    String first = freshAlias();
    String second = freshAlias();

    repository.recordClick(first);
    repository.recordClick(second);
    repository.recordClick(second);

    assertThat(repository.countClicks(first)).isEqualTo(1L);
    assertThat(repository.countClicks(second)).isEqualTo(2L);
  }
}

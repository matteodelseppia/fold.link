package link.fold.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import link.fold.domain.CreateOutcome;
import link.fold.domain.LookupResult;
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
 * Contract tests for the Redis adapter, run against a real, reachable Redis (see
 * scripts/dev/redis-start.sh / the CI "redis" service) - skipped, not failed, if none is reachable,
 * matching {@code link.fold.health}'s convention. Each test class run gets its own randomly
 * generated key prefix so parallel/CI runs can never share keys, and every key created is cleaned
 * up afterward.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class RedisUrlMappingRepositoryTests {

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

  @Autowired private RedisUrlMappingRepository repository;
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
  void firstCreateForAnAliasStoresItAndReturnsStored() {
    String alias = freshAlias();

    assertThat(repository.create(alias, "https://example.com/a")).isEqualTo(CreateOutcome.STORED);
    assertThat(repository.findByAlias(alias))
        .isEqualTo(new LookupResult.Found("https://example.com/a"));
  }

  @Test
  void secondCreateForTheSameAliasReturnsCollisionAndPreservesTheOriginalValue() {
    String alias = freshAlias();

    assertThat(repository.create(alias, "https://example.com/first"))
        .isEqualTo(CreateOutcome.STORED);
    assertThat(repository.create(alias, "https://example.com/second"))
        .isEqualTo(CreateOutcome.COLLISION);
    assertThat(repository.findByAlias(alias))
        .isEqualTo(new LookupResult.Found("https://example.com/first"));
  }

  @Test
  void storedKeysExpireAfterTheConfiguredTtl() {
    String alias = freshAlias();
    repository.create(alias, "https://example.com/a");

    Long ttl = redisTemplate.getExpire(keyCodec.toKey(alias));
    assertThat(ttl).isNotNull().isPositive().isLessThanOrEqualTo(Duration.ofDays(3).toSeconds());
  }

  @Test
  void lookupOfAMissingAliasReturnsNotFound() {
    assertThat(repository.findByAlias(freshAlias())).isInstanceOf(LookupResult.NotFound.class);
  }

  @Test
  void lookupPreservesUnicodeAndEncodedUrlsExactly() {
    String alias = freshAlias();
    String destination = "https://example.com/caf%C3%A9?q=%E2%9C%93#top";
    repository.create(alias, destination);

    assertThat(repository.findByAlias(alias)).isEqualTo(new LookupResult.Found(destination));
  }

  @Test
  void lookupOfMalformedStoredDataReturnsStorageFailure() {
    String alias = freshAlias();
    redisTemplate.opsForValue().set(keyCodec.toKey(alias), "not a valid destination url");

    assertThat(repository.findByAlias(alias)).isInstanceOf(LookupResult.StorageFailure.class);
  }
}

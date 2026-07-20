package link.fold.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import link.fold.config.AppProperties;
import link.fold.domain.CreateOutcome;
import link.fold.domain.DestinationValidator;
import link.fold.domain.LookupResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Points the repository at an address nothing listens on (no Spring context needed - just the plain
 * Lettuce connection factory) to verify Redis failures map to the storage-failure outcomes rather
 * than propagating a raw exception.
 */
class RedisUrlMappingRepositoryUnavailableTests {

  private static final AppProperties PROPERTIES =
      new AppProperties(
          "https://fold.link", new AppProperties.Alias(8, 5), new AppProperties.Redis("v1:link:"));

  private final LettuceConnectionFactory connectionFactory = unreachableConnectionFactory();
  private final StringRedisTemplate redisTemplate = newTemplate(connectionFactory);
  private final RedisUrlMappingRepository repository =
      new RedisUrlMappingRepository(
          redisTemplate, new RedisKeyCodec(PROPERTIES), new DestinationValidator());

  private static LettuceConnectionFactory unreachableConnectionFactory() {
    LettuceConnectionFactory factory =
        new LettuceConnectionFactory(
            new RedisStandaloneConfiguration("127.0.0.1", 19999),
            LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(300)).build());
    factory.afterPropertiesSet();
    return factory;
  }

  private static StringRedisTemplate newTemplate(LettuceConnectionFactory factory) {
    StringRedisTemplate template = new StringRedisTemplate(factory);
    template.afterPropertiesSet();
    return template;
  }

  @AfterEach
  void shutdown() {
    connectionFactory.destroy();
  }

  @Test
  void createReturnsStorageFailureWhenRedisIsUnreachable() {
    assertThat(repository.create("aaaaaaaa", "https://example.com"))
        .isEqualTo(CreateOutcome.STORAGE_FAILURE);
  }

  @Test
  void lookupReturnsStorageFailureWhenRedisIsUnreachable() {
    assertThat(repository.findByAlias("aaaaaaaa")).isInstanceOf(LookupResult.StorageFailure.class);
  }
}

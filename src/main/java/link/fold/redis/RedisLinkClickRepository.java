package link.fold.redis;

import java.time.Duration;
import link.fold.config.AppProperties;
import link.fold.domain.LinkClickRepository;
import link.fold.domain.StorageUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis-backed {@link LinkClickRepository}. Each click is an atomic {@code INCR} against {@code
 * v1:link:{alias}:clicks}, with its TTL refreshed to the configured mapping TTL on every increment,
 * so a click counter never outlives - or needs separate cleanup from - the mapping it describes.
 * Increment failures are logged and swallowed rather than propagated: a Redis hiccup while
 * recording a click must never turn a working redirect into an error response.
 */
@Repository
public class RedisLinkClickRepository implements LinkClickRepository {

  private static final Logger log = LoggerFactory.getLogger(RedisLinkClickRepository.class);

  private final StringRedisTemplate redisTemplate;
  private final RedisKeyCodec keyCodec;
  private final Duration ttl;

  public RedisLinkClickRepository(
      StringRedisTemplate redisTemplate, RedisKeyCodec keyCodec, AppProperties properties) {
    this.redisTemplate = redisTemplate;
    this.keyCodec = keyCodec;
    this.ttl = properties.redis().ttl();
  }

  @Override
  public void recordClick(String alias) {
    try {
      String key = keyCodec.toClickKey(alias);
      redisTemplate.opsForValue().increment(key);
      redisTemplate.expire(key, ttl);
    } catch (DataAccessException e) {
      log.warn("Redis click increment failed for alias={}", alias, e);
    }
  }

  @Override
  public long countClicks(String alias) {
    String key = keyCodec.toClickKey(alias);
    try {
      String value = redisTemplate.opsForValue().get(key);
      return value == null ? 0L : Long.parseLong(value);
    } catch (DataAccessException e) {
      log.warn("Redis click count failed for alias={}", alias, e);
      throw new StorageUnavailableException("Unable to count clicks: storage failure");
    }
  }
}

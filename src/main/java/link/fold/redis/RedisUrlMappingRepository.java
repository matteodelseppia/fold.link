package link.fold.redis;

import link.fold.domain.CreateOutcome;
import link.fold.domain.DestinationValidator;
import link.fold.domain.LookupResult;
import link.fold.domain.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis-backed {@link UrlMappingRepository}. Creation uses {@code SET NX} so concurrent requests
 * for the same alias can never overwrite each other's value, and writes carry no expiry (ADR-001:
 * mappings persist until explicitly removed). Repository failures never leak Redis exceptions,
 * hosts, or keys to callers - they collapse to {@link CreateOutcome#STORAGE_FAILURE} / {@link
 * LookupResult.StorageFailure}, logged here with only the alias for diagnostics.
 */
@Repository
public class RedisUrlMappingRepository implements UrlMappingRepository {

  private static final Logger log = LoggerFactory.getLogger(RedisUrlMappingRepository.class);

  private final StringRedisTemplate redisTemplate;
  private final RedisKeyCodec keyCodec;
  private final DestinationValidator destinationValidator;

  public RedisUrlMappingRepository(
      StringRedisTemplate redisTemplate,
      RedisKeyCodec keyCodec,
      DestinationValidator destinationValidator) {
    this.redisTemplate = redisTemplate;
    this.keyCodec = keyCodec;
    this.destinationValidator = destinationValidator;
  }

  @Override
  public CreateOutcome create(String alias, String destination) {
    try {
      String key = keyCodec.toKey(alias);
      Boolean stored = redisTemplate.opsForValue().setIfAbsent(key, destination);
      return Boolean.TRUE.equals(stored) ? CreateOutcome.STORED : CreateOutcome.COLLISION;
    } catch (DataAccessException e) {
      log.warn("Redis create failed for alias={}", alias, e);
      return CreateOutcome.STORAGE_FAILURE;
    }
  }

  @Override
  public LookupResult findByAlias(String alias) {
    String key;
    try {
      key = keyCodec.toKey(alias);
    } catch (IllegalArgumentException invalidShape) {
      return new LookupResult.NotFound();
    }

    String value;
    try {
      value = redisTemplate.opsForValue().get(key);
    } catch (DataAccessException e) {
      log.warn("Redis lookup failed for alias={}", alias, e);
      return new LookupResult.StorageFailure();
    }

    if (value == null) {
      return new LookupResult.NotFound();
    }
    if (!destinationValidator.isValidCanonical(value)) {
      log.warn("Malformed stored destination for alias={}", alias);
      return new LookupResult.StorageFailure();
    }
    return new LookupResult.Found(value);
  }
}

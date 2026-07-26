package link.fold.redis;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.DataFormatException;
import link.fold.config.AppProperties;
import link.fold.domain.CreateOutcome;
import link.fold.domain.DestinationValidator;
import link.fold.domain.LookupResult;
import link.fold.domain.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis-backed {@link UrlMappingRepository}. Creation uses {@code SET NX} so concurrent requests
 * for the same alias can never overwrite each other's value, and writes carry the configured TTL
 * ({@code app.redis.ttl}, defaulting to 30 days) so mappings expire automatically instead of
 * accumulating forever. Repository failures never leak Redis exceptions, hosts, or keys to callers
 * - they collapse to {@link CreateOutcome#STORAGE_FAILURE} / {@link LookupResult.StorageFailure},
 * logged here with only the alias for diagnostics.
 *
 * <p>Destinations are stored compressed (see {@link DestinationCodec}) to cut per-mapping memory
 * use. A value that fails to decompress is assumed to predate compression and is read back as plain
 * UTF-8 text instead, so mappings written before this change keep resolving without a migration
 * step.
 */
@Repository
public class RedisUrlMappingRepository implements UrlMappingRepository {

  private static final Logger log = LoggerFactory.getLogger(RedisUrlMappingRepository.class);

  private final RedisTemplate<String, byte[]> redisTemplate;
  private final RedisKeyCodec keyCodec;
  private final DestinationCodec destinationCodec;
  private final DestinationValidator destinationValidator;
  private final Duration ttl;

  public RedisUrlMappingRepository(
      @Qualifier("destinationRedisTemplate") RedisTemplate<String, byte[]> redisTemplate,
      RedisKeyCodec keyCodec,
      DestinationCodec destinationCodec,
      DestinationValidator destinationValidator,
      AppProperties properties) {
    this.redisTemplate = redisTemplate;
    this.keyCodec = keyCodec;
    this.destinationCodec = destinationCodec;
    this.destinationValidator = destinationValidator;
    this.ttl = properties.redis().ttl();
  }

  @Override
  public CreateOutcome create(String alias, String destination) {
    try {
      String key = keyCodec.toKey(alias);
      byte[] compressed = destinationCodec.compress(destination);
      Boolean stored = redisTemplate.opsForValue().setIfAbsent(key, compressed, ttl);
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

    byte[] stored;
    try {
      stored = redisTemplate.opsForValue().get(key);
    } catch (DataAccessException e) {
      log.warn("Redis lookup failed for alias={}", alias, e);
      return new LookupResult.StorageFailure();
    }

    if (stored == null) {
      return new LookupResult.NotFound();
    }

    String destination = decode(stored);
    if (destination == null) {
      log.warn("Malformed stored destination for alias={}", alias);
      return new LookupResult.StorageFailure();
    }
    return new LookupResult.Found(destination);
  }

  private String decode(byte[] stored) {
    String decompressed = tryDecompress(stored);
    if (decompressed != null && destinationValidator.isValidCanonical(decompressed)) {
      return decompressed;
    }

    String legacy = new String(stored, StandardCharsets.UTF_8);
    return destinationValidator.isValidCanonical(legacy) ? legacy : null;
  }

  private String tryDecompress(byte[] stored) {
    try {
      return destinationCodec.decompress(stored);
    } catch (DataFormatException notCompressed) {
      return null;
    }
  }
}

package link.fold.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * A byte-valued {@link RedisTemplate} for the compressed destination bytes written by {@code
 * RedisUrlMappingRepository}. Kept separate from Spring Boot's auto-configured {@code
 * StringRedisTemplate} (still used everywhere else, e.g. click counters): compressed values are
 * arbitrary bytes, not UTF-8 text, so routing them through a String serializer would either corrupt
 * them or force a base64 detour that gives back a third of the compression savings.
 */
@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<String, byte[]> destinationRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, byte[]> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(RedisSerializer.string());
    template.setValueSerializer(RedisSerializer.byteArray());
    template.afterPropertiesSet();
    return template;
  }
}

package link.fold.redis;

import java.util.regex.Pattern;
import link.fold.config.AppProperties;
import org.springframework.stereotype.Component;

/**
 * Converts an alias into its Redis key: the configured prefix (schema version + namespace, e.g.
 * {@code v1:link:}) followed by the alias, per ADR-001. Only aliases matching the configured
 * contract shape can produce a key - the alphabet excludes {@code :}, so a value containing a
 * (possibly doubled) prefix can never itself be encoded as a valid alias.
 */
@Component
public class RedisKeyCodec {

  private final String keyPrefix;
  private final Pattern aliasPattern;

  public RedisKeyCodec(AppProperties properties) {
    this.keyPrefix = properties.redis().keyPrefix();
    this.aliasPattern = Pattern.compile("^[A-Za-z0-9_-]{" + properties.alias().length() + "}$");
  }

  public String toKey(String alias) {
    if (alias == null || !aliasPattern.matcher(alias).matches()) {
      throw new IllegalArgumentException("alias does not match the contract shape");
    }
    return keyPrefix + alias;
  }
}

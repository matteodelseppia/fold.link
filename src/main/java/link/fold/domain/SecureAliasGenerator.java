package link.fold.domain;

import java.security.SecureRandom;
import java.util.function.IntFunction;
import link.fold.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generates aliases as {@code length} URL-safe Base64 characters, per ADR-001. Each byte of
 * cryptographically secure randomness is masked to its low 6 bits and mapped straight into the
 * 64-character alphabet; since 64 is a power of two, every alphabet character is equally likely and
 * no modulo-bias correction is needed. No timestamp, counter, or destination-derived data is ever
 * part of the alias.
 */
@Component
public class SecureAliasGenerator implements AliasGenerator {

  static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final int length;
  private final IntFunction<byte[]> randomBytes;

  @Autowired
  public SecureAliasGenerator(AppProperties properties) {
    this(
        properties.alias().length(),
        count -> {
          byte[] bytes = new byte[count];
          SECURE_RANDOM.nextBytes(bytes);
          return bytes;
        });
  }

  SecureAliasGenerator(int length, IntFunction<byte[]> randomBytes) {
    this.length = length;
    this.randomBytes = randomBytes;
  }

  @Override
  public String generate() {
    byte[] bytes = randomBytes.apply(length);
    StringBuilder alias = new StringBuilder(length);
    for (byte b : bytes) {
      alias.append(ALPHABET.charAt(b & 0x3F));
    }
    return alias.toString();
  }
}

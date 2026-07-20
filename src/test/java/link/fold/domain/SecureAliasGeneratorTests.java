package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;

class SecureAliasGeneratorTests {

  @Test
  void mapsInjectedBytesThroughLowSixBitsIntoAlphabet() {
    // 0x00 -> 'A', 0x3F -> '_', 0x40 -> 'A' again (only the low 6 bits matter).
    IntFunction<byte[]> fixedBytes = count -> new byte[] {0x00, 0x3F, 0x40, 0x01};
    SecureAliasGenerator generator = new SecureAliasGenerator(4, fixedBytes);

    assertThat(generator.generate()).isEqualTo("A_AB");
  }

  @Test
  void producesConfiguredLength() {
    IntFunction<byte[]> fixedBytes = count -> new byte[count];
    SecureAliasGenerator generator = new SecureAliasGenerator(12, fixedBytes);

    assertThat(generator.generate()).hasSize(12);
  }

  @Test
  void everyCharacterComesFromTheDocumentedAlphabet() {
    IntFunction<byte[]> allBytesSource =
        count -> {
          byte[] bytes = new byte[count];
          for (int i = 0; i < count; i++) {
            bytes[i] = (byte) i;
          }
          return bytes;
        };
    SecureAliasGenerator generator = new SecureAliasGenerator(256, allBytesSource);

    String alias = generator.generate();
    for (char c : alias.toCharArray()) {
      assertThat(SecureAliasGenerator.ALPHABET).contains(String.valueOf(c));
    }
  }

  @Test
  void statisticalSmokeTestCatchesConstantOutputRegression() {
    SecureAliasGenerator realGenerator =
        new SecureAliasGenerator(
            8,
            count -> {
              byte[] bytes = new byte[count];
              new java.security.SecureRandom().nextBytes(bytes);
              return bytes;
            });

    int sampleSize = 2000;
    Set<String> generated = new HashSet<>();
    for (int i = 0; i < sampleSize; i++) {
      String alias = realGenerator.generate();
      assertThat(alias).hasSize(8);
      assertThat(alias).matches("[A-Za-z0-9_-]{8}");
      generated.add(alias);
    }

    // With a 64^8 alias space, 2000 samples colliding at all would be a
    // near-impossible coincidence; a near-1:1 unique ratio is what a
    // constant-output or badly-broken generator would fail.
    assertThat(generated).hasSizeGreaterThan((int) (sampleSize * 0.99));
  }

  @Test
  void constantOutputFakeGeneratorFailsTheStatisticalSmokeTest() {
    SecureAliasGenerator constantGenerator = new SecureAliasGenerator(8, count -> new byte[count]);

    Set<String> generated = new HashSet<>();
    for (int i = 0; i < 50; i++) {
      generated.add(constantGenerator.generate());
    }

    assertThat(generated).hasSize(1);
  }
}

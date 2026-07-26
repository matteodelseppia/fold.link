package link.fold.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DestinationCodecTests {

  private final DestinationCodec codec = new DestinationCodec();

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://example.com/a",
        "https://github.com/anthropics/claude-code",
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ&utm_source=share&utm_medium=web",
        "https://example.com/caf%C3%A9?q=%E2%9C%93#top",
        "http://a.io/x"
      })
  void compressThenDecompressRoundTripsExactly(String destination) throws DataFormatException {
    byte[] compressed = codec.compress(destination);

    assertThat(codec.decompress(compressed)).isEqualTo(destination);
  }

  @Test
  void typicalUrlsFromCommonDomainsCompressSmallerThanTheirUtf8Encoding() {
    String destination =
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ&utm_source=share&utm_medium=web";

    byte[] compressed = codec.compress(destination);

    assertThat(compressed.length).isLessThan(destination.getBytes(StandardCharsets.UTF_8).length);
  }

  @Test
  void decompressOfArbitraryTextThrowsDataFormatException() {
    byte[] notCompressed = "not a valid destination url".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> codec.decompress(notCompressed))
        .isInstanceOf(DataFormatException.class);
  }
}

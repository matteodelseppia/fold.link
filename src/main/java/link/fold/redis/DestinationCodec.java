package link.fold.redis;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.springframework.stereotype.Component;

/**
 * Compresses destination URLs with raw DEFLATE (RFC 1951, no zlib/gzip header) primed with a shared
 * dictionary of common URL tokens - schemes, popular TLDs and domains, tracking params.
 *
 * <p>A short URL has little or no redundancy of its own for a generic compressor to exploit, and
 * per-message format overhead (a zlib header, a gzip header, base64 padding) can easily outweigh
 * whatever it does find, growing the value instead of shrinking it. Priming the compressor with a
 * dictionary of substrings that recur *across* URLs - not within any single one - fixes that: the
 * scheme, the leading {@code www.}, the TLD, and common tracking query params typically compress to
 * a couple of backreferences instead of being stored literally. This is the same principle behind
 * shared-dictionary compression schemes used for small, self-similar messages elsewhere (e.g. HTTP
 * header compression), applied here with only the JDK's built-in {@link Deflater}.
 */
@Component
public class DestinationCodec {

  // Ordered least- to most-common so the hottest tokens sit at the end of the window, closest to
  // the data being compressed and so cheapest for DEFLATE to reference.
  private static final byte[] DICTIONARY =
      ("gclid=fbclid=igshid="
              + "utm_content=utm_term=utm_campaign=utm_medium=utm_source="
              + "?ref=&ref=/index.html/search?q=/watch?v="
              + ".me/.app/.dev/.io/.co/.gov/.edu/"
              + "reddit.comwikipedia.orglinkedin.cominstagram.com"
              + "twitter.comx.comamazon.comgithub.com"
              + "youtube.comfacebook.comgoogle.com"
              + ".net/.org/.com/"
              + "http://https://http://www.https://www.")
          .getBytes(StandardCharsets.UTF_8);

  private static final int BUFFER_SIZE = 256;

  /** Compresses {@code destination} into raw DEFLATE bytes. */
  public byte[] compress(String destination) {
    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
    try {
      deflater.setDictionary(DICTIONARY);
      deflater.setInput(destination.getBytes(StandardCharsets.UTF_8));
      deflater.finish();

      ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
      byte[] buffer = new byte[BUFFER_SIZE];
      while (!deflater.finished()) {
        int written = deflater.deflate(buffer);
        out.write(buffer, 0, written);
      }
      return out.toByteArray();
    } finally {
      deflater.end();
    }
  }

  /**
   * Reverses {@link #compress(String)}. Throws {@link DataFormatException} if {@code compressed} is
   * not a valid raw-DEFLATE stream produced with the same dictionary - callers use this to detect
   * values that predate compression and fall back to treating them as plain text.
   */
  public String decompress(byte[] compressed) throws DataFormatException {
    Inflater inflater = new Inflater(true);
    try {
      // Raw (nowrap) DEFLATE has no header to signal that a dictionary is needed - unlike zlib's
      // FDICT flag - so it must be primed unconditionally before the first inflate() call.
      inflater.setDictionary(DICTIONARY);
      inflater.setInput(compressed);

      ByteArrayOutputStream out =
          new ByteArrayOutputStream(Math.max(BUFFER_SIZE, compressed.length * 3));
      byte[] buffer = new byte[BUFFER_SIZE];
      while (!inflater.finished()) {
        int read = inflater.inflate(buffer);
        if (read == 0) {
          if (inflater.needsInput() || inflater.needsDictionary()) {
            throw new DataFormatException("truncated deflate stream");
          }
        } else {
          out.write(buffer, 0, read);
        }
      }
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    } finally {
      inflater.end();
    }
  }
}

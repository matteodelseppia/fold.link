package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * Round-trips real PNG bytes back through a ZXing reader (test-only dependency, see build.gradle)
 * rather than asserting on internal implementation details - this is the only way to actually prove
 * the encoder produces a scannable code, not just "some bytes that look like a PNG."
 */
class ZxingQrCodeServiceTests {

  private final ZxingQrCodeService service = new ZxingQrCodeService();

  @Test
  void generatePngProducesAScannableCodeThatDecodesBackToTheOriginalContent() throws Exception {
    String content = "https://fold.link/abc12345";

    byte[] png = service.generatePng(content);

    assertThat(decode(png)).isEqualTo(content);
  }

  @Test
  void generatePngProducesAValidPngWithTheExpectedMagicBytes() {
    byte[] png = service.generatePng("https://fold.link/abc12345");

    byte[] pngMagic = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    assertThat(png).startsWith(pngMagic);
  }

  @Test
  void generatePngProducesASquareImage() throws IOException {
    byte[] png = service.generatePng("https://fold.link/abc12345");

    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
    assertThat(image.getWidth()).isEqualTo(image.getHeight());
    assertThat(image.getWidth()).isPositive();
  }

  @Test
  void differentContentProducesDifferentImages() {
    byte[] first = service.generatePng("https://fold.link/aaaaaaaa");
    byte[] second = service.generatePng("https://fold.link/bbbbbbbb");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void contentExceedingQrCapacityThrowsQrGenerationException() {
    String tooLong = "https://fold.link/" + "a".repeat(5000);

    assertThatThrownBy(() -> service.generatePng(tooLong))
        .isInstanceOf(QrGenerationException.class)
        .hasCauseInstanceOf(com.google.zxing.WriterException.class);
  }

  private static String decode(byte[] png) throws Exception {
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
    BinaryBitmap bitmap =
        new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
    return new QRCodeReader().decode(bitmap).getText();
  }
}

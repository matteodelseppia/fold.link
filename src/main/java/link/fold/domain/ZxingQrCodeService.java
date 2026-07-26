package link.fold.domain;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/**
 * Encodes text (in practice, a short link's own public URL) as a square PNG QR code using ZXing.
 * Rendering to PNG is done by hand with {@link BufferedImage}/{@link ImageIO} rather than pulling
 * in {@code zxing-javase}, so the encoder's only runtime dependency is {@code zxing-core}.
 *
 * <p>Encoding a short, fixed-length URL is cheap - on the order of a millisecond - so nothing here
 * is cached; every request re-encodes.
 */
@Component
public class ZxingQrCodeService implements QrCodeService {

  private static final int SIZE_PX = 320;
  private static final int QUIET_ZONE_MODULES = 2;
  private static final String PNG_FORMAT_NAME = "png";
  private static final int BLACK_RGB = 0x000000;
  private static final int WHITE_RGB = 0xFFFFFF;

  @Override
  public byte[] generatePng(String content) {
    BitMatrix matrix = encode(content);
    BufferedImage image = toImage(matrix);
    return toPngBytes(image);
  }

  private static BitMatrix encode(String content) {
    Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    hints.put(EncodeHintType.MARGIN, QUIET_ZONE_MODULES);
    try {
      return new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX, hints);
    } catch (WriterException e) {
      throw new QrGenerationException("Unable to encode content as a QR code", e);
    }
  }

  private static BufferedImage toImage(BitMatrix matrix) {
    int width = matrix.getWidth();
    int height = matrix.getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        image.setRGB(x, y, matrix.get(x, y) ? BLACK_RGB : WHITE_RGB);
      }
    }
    return image;
  }

  private static byte[] toPngBytes(BufferedImage image) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      ImageIO.write(image, PNG_FORMAT_NAME, out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new QrGenerationException("Unable to render the QR code as PNG", e);
    }
  }
}

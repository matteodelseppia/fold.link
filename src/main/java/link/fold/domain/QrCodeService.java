package link.fold.domain;

/** Encodes text content into a QR code image. */
public interface QrCodeService {

  /**
   * Encodes {@code content} as a QR code and returns it as PNG image bytes.
   *
   * @throws QrGenerationException if the content cannot be encoded (e.g. it exceeds QR code
   *     capacity) or the resulting image cannot be rendered as PNG.
   */
  byte[] generatePng(String content);
}

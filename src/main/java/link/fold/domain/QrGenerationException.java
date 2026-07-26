package link.fold.domain;

/** A QR code could not be generated for the given content. */
public class QrGenerationException extends RuntimeException {

  public QrGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}

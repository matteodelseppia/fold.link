package link.fold.domain;

/** A destination URL failed scheme, host, or shape validation. Carries a user-safe message. */
public class InvalidDestinationException extends RuntimeException {

  public InvalidDestinationException(String message) {
    super(message);
  }
}

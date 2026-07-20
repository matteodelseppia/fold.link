package link.fold.domain;

/** The repository could not complete a create or lookup, or alias retries were exhausted. */
public class StorageUnavailableException extends RuntimeException {

  public StorageUnavailableException(String message) {
    super(message);
  }
}

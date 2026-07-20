package link.fold.domain;

/** No link mapping exists for the requested alias, or the alias never had a valid shape. */
public class AliasNotFoundException extends RuntimeException {

  public AliasNotFoundException(String alias) {
    super("No link exists for alias: " + alias);
  }
}

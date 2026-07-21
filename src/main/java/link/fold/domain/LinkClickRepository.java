package link.fold.domain;

/**
 * Domain-facing port for recording and counting clicks on an alias. Implementations (e.g. Redis)
 * never leak their storage-specific types across this boundary.
 */
public interface LinkClickRepository {

  /** Records one click for {@code alias}. */
  void recordClick(String alias);

  /** Returns the number of clicks recorded for {@code alias}, or {@code 0} if none have. */
  long countClicks(String alias);
}

package link.fold.domain;

/**
 * Domain-facing port for persisting and resolving alias-to-destination mappings. Implementations
 * (e.g. Redis) never leak their storage-specific types across this boundary.
 */
public interface UrlMappingRepository {

  /** Atomically stores {@code alias -> destination} only if {@code alias} is not already taken. */
  CreateOutcome create(String alias, String destination);

  /** Resolves {@code alias} to its stored destination, if any. */
  LookupResult findByAlias(String alias);
}

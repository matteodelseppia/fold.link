package link.fold.domain;

/** Distinct outcomes of resolving an alias through the {@link UrlMappingRepository}. */
public sealed interface LookupResult {

  record Found(String destination) implements LookupResult {}

  record NotFound() implements LookupResult {}

  record StorageFailure() implements LookupResult {}
}

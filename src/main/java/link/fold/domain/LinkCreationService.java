package link.fold.domain;

import link.fold.config.AppProperties;
import org.springframework.stereotype.Service;

/**
 * Validates and canonicalizes a destination, then generates and stores an alias for it, retrying a
 * bounded number of times on alias collisions (ADR-001: 8-character Base64url aliases).
 *
 * <p>Validation runs before any alias is generated or the repository is touched, so invalid input
 * never costs a Redis round trip.
 */
@Service
public class LinkCreationService {

  private final DestinationValidator destinationValidator;
  private final AliasGenerator aliasGenerator;
  private final UrlMappingRepository repository;
  private final int retryCount;

  public LinkCreationService(
      DestinationValidator destinationValidator,
      AliasGenerator aliasGenerator,
      UrlMappingRepository repository,
      AppProperties properties) {
    this.destinationValidator = destinationValidator;
    this.aliasGenerator = aliasGenerator;
    this.repository = repository;
    this.retryCount = properties.alias().retryCount();
  }

  public LinkMapping createLink(String rawUrl) {
    String canonicalDestination = destinationValidator.validateAndCanonicalize(rawUrl);

    for (int attempt = 0; attempt < retryCount; attempt++) {
      String alias = aliasGenerator.generate();
      CreateOutcome outcome = repository.create(alias, canonicalDestination);
      switch (outcome) {
        case STORED -> {
          return new LinkMapping(alias, canonicalDestination);
        }
        case COLLISION -> {
          // try again with a freshly generated alias
        }
        case STORAGE_FAILURE ->
            throw new StorageUnavailableException("Unable to store link: storage failure");
      }
    }

    throw new StorageUnavailableException("Unable to store link: alias retries exhausted");
  }
}

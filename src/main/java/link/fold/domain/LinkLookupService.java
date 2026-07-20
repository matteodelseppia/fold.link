package link.fold.domain;

import java.util.regex.Pattern;
import link.fold.config.AppProperties;
import org.springframework.stereotype.Service;

/**
 * Resolves a path alias to its stored destination. Aliases that don't even match the contract shape
 * (length and alphabet, ADR-001) are treated as not-found without ever reaching the repository -
 * there is no valid mapping they could possibly resolve to.
 */
@Service
public class LinkLookupService {

  private final UrlMappingRepository repository;
  private final Pattern aliasPattern;

  public LinkLookupService(UrlMappingRepository repository, AppProperties properties) {
    this.repository = repository;
    this.aliasPattern = Pattern.compile("^[A-Za-z0-9_-]{" + properties.alias().length() + "}$");
  }

  public String resolve(String alias) {
    if (alias == null || !aliasPattern.matcher(alias).matches()) {
      throw new AliasNotFoundException(alias);
    }

    LookupResult result = repository.findByAlias(alias);
    if (result instanceof LookupResult.Found found) {
      return found.destination();
    }
    if (result instanceof LookupResult.NotFound) {
      throw new AliasNotFoundException(alias);
    }
    throw new StorageUnavailableException("Unable to resolve alias: storage failure");
  }
}

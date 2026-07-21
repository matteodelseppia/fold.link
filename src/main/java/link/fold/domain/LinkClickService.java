package link.fold.domain;

import java.util.regex.Pattern;
import link.fold.config.AppProperties;
import org.springframework.stereotype.Service;

/**
 * Records and reports click counts for aliases. Aliases that don't even match the contract shape
 * (length and alphabet, ADR-001) are treated the same way {@link LinkLookupService} treats them -
 * there is no valid mapping they could ever count clicks for, so a click is silently dropped and a
 * count lookup is a 404 without ever reaching the repository.
 */
@Service
public class LinkClickService {

  private final LinkClickRepository repository;
  private final Pattern aliasPattern;

  public LinkClickService(LinkClickRepository repository, AppProperties properties) {
    this.repository = repository;
    this.aliasPattern = Pattern.compile("^[A-Za-z0-9_-]{" + properties.alias().length() + "}$");
  }

  public void recordClick(String alias) {
    if (alias == null || !aliasPattern.matcher(alias).matches()) {
      return;
    }
    repository.recordClick(alias);
  }

  public long countClicks(String alias) {
    if (alias == null || !aliasPattern.matcher(alias).matches()) {
      throw new AliasNotFoundException(alias);
    }
    return repository.countClicks(alias);
  }
}

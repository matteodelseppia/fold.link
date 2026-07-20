package link.fold.domain;

/**
 * An alias and the canonical destination it resolves to. Construction enforces both invariants so
 * no other layer needs to re-check blankness.
 */
public record LinkMapping(String alias, String destination) {

  public LinkMapping {
    if (alias == null || alias.isBlank()) {
      throw new IllegalArgumentException("alias must not be blank");
    }
    if (destination == null || destination.isBlank()) {
      throw new IllegalArgumentException("destination must not be blank");
    }
  }
}

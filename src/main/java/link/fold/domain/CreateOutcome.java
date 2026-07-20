package link.fold.domain;

/** Distinct outcomes of an atomic create-if-absent write to the {@link UrlMappingRepository}. */
public enum CreateOutcome {
  STORED,
  COLLISION,
  STORAGE_FAILURE
}

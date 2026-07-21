package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import link.fold.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkLookupServiceTests {

  private static final AppProperties PROPERTIES =
      new AppProperties(
          "https://fold.link",
          new AppProperties.Alias(8, 3),
          new AppProperties.Redis("v1:link:", Duration.ofDays(3)));

  @Mock private UrlMappingRepository repository;

  private LinkLookupService service;

  @BeforeEach
  void setUp() {
    service = new LinkLookupService(repository, PROPERTIES);
  }

  @Test
  void returnsDestinationOnHit() {
    when(repository.findByAlias("abc12345"))
        .thenReturn(new LookupResult.Found("https://example.com"));

    assertThat(service.resolve("abc12345")).isEqualTo("https://example.com");
  }

  @Test
  void throwsAliasNotFoundOnMiss() {
    when(repository.findByAlias("abc12345")).thenReturn(new LookupResult.NotFound());

    assertThatThrownBy(() -> service.resolve("abc12345"))
        .isInstanceOf(AliasNotFoundException.class);
  }

  @Test
  void throwsStorageUnavailableOnRepositoryFailure() {
    when(repository.findByAlias("abc12345")).thenReturn(new LookupResult.StorageFailure());

    assertThatThrownBy(() -> service.resolve("abc12345"))
        .isInstanceOf(StorageUnavailableException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "short", "waytoolongofanalias", "has space", "has:colon"})
  void invalidAliasShapesAreNotFoundWithoutQueryingTheRepository(String invalidAlias) {
    assertThatThrownBy(() -> service.resolve(invalidAlias))
        .isInstanceOf(AliasNotFoundException.class);

    verifyNoInteractions(repository);
  }

  @Test
  void nullAliasIsNotFoundWithoutQueryingTheRepository() {
    assertThatThrownBy(() -> service.resolve(null)).isInstanceOf(AliasNotFoundException.class);

    verifyNoInteractions(repository);
  }
}

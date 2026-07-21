package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
class LinkClickServiceTests {

  private static final AppProperties PROPERTIES =
      new AppProperties(
          "https://fold.link",
          new AppProperties.Alias(8, 3),
          new AppProperties.Redis("v1:link:", Duration.ofDays(3)));

  @Mock private LinkClickRepository repository;

  private LinkClickService service;

  @BeforeEach
  void setUp() {
    service = new LinkClickService(repository, PROPERTIES);
  }

  @Test
  void recordClickDelegatesToTheRepositoryForAValidAlias() {
    service.recordClick("abc12345");

    verify(repository).recordClick("abc12345");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "short", "waytoolongofanalias", "has space", "has:colon"})
  void recordClickIgnoresInvalidAliasShapesWithoutQueryingTheRepository(String invalidAlias) {
    service.recordClick(invalidAlias);

    verifyNoInteractions(repository);
  }

  @Test
  void recordClickIgnoresNullAliasWithoutQueryingTheRepository() {
    service.recordClick(null);

    verifyNoInteractions(repository);
  }

  @Test
  void countClicksReturnsTheRepositoryCount() {
    when(repository.countClicks("abc12345")).thenReturn(42L);

    assertThat(service.countClicks("abc12345")).isEqualTo(42L);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "short", "waytoolongofanalias", "has space", "has:colon"})
  void countClicksThrowsAliasNotFoundForInvalidShapesWithoutQueryingTheRepository(
      String invalidAlias) {
    assertThatThrownBy(() -> service.countClicks(invalidAlias))
        .isInstanceOf(AliasNotFoundException.class);

    verifyNoInteractions(repository);
  }

  @Test
  void countClicksThrowsAliasNotFoundForNullAliasWithoutQueryingTheRepository() {
    assertThatThrownBy(() -> service.countClicks(null)).isInstanceOf(AliasNotFoundException.class);

    verifyNoInteractions(repository);
  }
}

package link.fold.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import link.fold.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkCreationServiceTests {

  private static final AppProperties PROPERTIES =
      new AppProperties(
          "https://fold.link",
          new AppProperties.Alias(8, 3),
          new AppProperties.Redis("v1:link:", Duration.ofDays(3)));

  @Mock private DestinationValidator destinationValidator;
  @Mock private AliasGenerator aliasGenerator;
  @Mock private UrlMappingRepository repository;

  private LinkCreationService service;

  @BeforeEach
  void setUp() {
    service = new LinkCreationService(destinationValidator, aliasGenerator, repository, PROPERTIES);
  }

  @Test
  void validatesBeforeGeneratingAnAliasOrTouchingTheRepository() {
    when(destinationValidator.validateAndCanonicalize("not-a-url"))
        .thenThrow(new InvalidDestinationException("bad url"));

    assertThatThrownBy(() -> service.createLink("not-a-url"))
        .isInstanceOf(InvalidDestinationException.class);

    verifyNoInteractions(aliasGenerator);
    verifyNoInteractions(repository);
  }

  @Test
  void returnsStoredMappingOnFirstSuccess() {
    when(destinationValidator.validateAndCanonicalize("https://example.com"))
        .thenReturn("https://example.com");
    when(aliasGenerator.generate()).thenReturn("abc12345");
    when(repository.create("abc12345", "https://example.com")).thenReturn(CreateOutcome.STORED);

    LinkMapping result = service.createLink("https://example.com");

    assertThat(result.alias()).isEqualTo("abc12345");
    assertThat(result.destination()).isEqualTo("https://example.com");

    InOrder order = inOrder(destinationValidator, aliasGenerator, repository);
    order.verify(destinationValidator).validateAndCanonicalize("https://example.com");
    order.verify(aliasGenerator).generate();
    order.verify(repository).create("abc12345", "https://example.com");
  }

  @Test
  void retriesWithANewAliasAfterCollisionsThenSucceeds() {
    when(destinationValidator.validateAndCanonicalize(anyString()))
        .thenReturn("https://example.com");
    when(aliasGenerator.generate()).thenReturn("aaaaaaaa", "bbbbbbbb", "cccccccc");
    when(repository.create("aaaaaaaa", "https://example.com")).thenReturn(CreateOutcome.COLLISION);
    when(repository.create("bbbbbbbb", "https://example.com")).thenReturn(CreateOutcome.COLLISION);
    when(repository.create("cccccccc", "https://example.com")).thenReturn(CreateOutcome.STORED);

    LinkMapping result = service.createLink("https://example.com");

    assertThat(result.alias()).isEqualTo("cccccccc");
    verify(repository, org.mockito.Mockito.times(3)).create(anyString(), any());
  }

  @Test
  void throwsStorageUnavailableWhenRetriesAreExhausted() {
    when(destinationValidator.validateAndCanonicalize(anyString()))
        .thenReturn("https://example.com");
    when(aliasGenerator.generate()).thenReturn("aaaaaaaa", "bbbbbbbb", "cccccccc");
    when(repository.create(anyString(), any())).thenReturn(CreateOutcome.COLLISION);

    assertThatThrownBy(() -> service.createLink("https://example.com"))
        .isInstanceOf(StorageUnavailableException.class);

    verify(repository, org.mockito.Mockito.times(3)).create(anyString(), any());
  }

  @Test
  void storageFailureIsNotRetriedAsACollision() {
    when(destinationValidator.validateAndCanonicalize(anyString()))
        .thenReturn("https://example.com");
    when(aliasGenerator.generate()).thenReturn("aaaaaaaa");
    when(repository.create("aaaaaaaa", "https://example.com"))
        .thenReturn(CreateOutcome.STORAGE_FAILURE);

    assertThatThrownBy(() -> service.createLink("https://example.com"))
        .isInstanceOf(StorageUnavailableException.class);

    verify(repository, org.mockito.Mockito.times(1)).create(anyString(), any());
    verify(aliasGenerator, org.mockito.Mockito.times(1)).generate();
  }
}

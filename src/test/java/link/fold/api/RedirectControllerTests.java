package link.fold.api;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import link.fold.domain.AliasNotFoundException;
import link.fold.domain.LinkClickService;
import link.fold.domain.LinkLookupService;
import link.fold.domain.StorageUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MVC tests for {@code GET /{alias}}: redirect status/Location for HTTP and HTTPS destinations with
 * encoded paths/queries/fragments, the missing/invalid-alias 404 path, and the storage-failure 503
 * path.
 */
@WebMvcTest(controllers = RedirectController.class)
class RedirectControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LinkLookupService linkLookupService;

  @MockitoBean private LinkClickService linkClickService;

  @Test
  void hitReturns302WithLocationHeader() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/dest");

    mockMvc
        .perform(get("/abc12345"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com/dest"));
  }

  @Test
  void hitRecordsAClickForTheResolvedAlias() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/dest");

    mockMvc.perform(get("/abc12345"));

    verify(linkClickService).recordClick("abc12345");
  }

  @Test
  void preservesEncodedPathsQueriesAndFragments() throws Exception {
    String destination = "https://example.com/a%20b?x=1&y=2#frag";
    when(linkLookupService.resolve("abc12345")).thenReturn(destination);

    mockMvc
        .perform(get("/abc12345"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", destination));
  }

  @Test
  void supportsHttpDestinationsToo() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("http://example.com/dest");

    mockMvc
        .perform(get("/abc12345"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "http://example.com/dest"));
  }

  @Test
  void responseBodyDoesNotLeakMappingData() throws Exception {
    when(linkLookupService.resolve("abc12345"))
        .thenReturn("https://example.com/secret-internal-path");

    mockMvc
        .perform(get("/abc12345"))
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
              org.assertj.core.api.Assertions.assertThat(body).isEmpty();
            });
  }

  @Test
  void unknownAliasReturns404() throws Exception {
    when(linkLookupService.resolve("unknown1")).thenThrow(new AliasNotFoundException("unknown1"));

    mockMvc.perform(get("/unknown1")).andExpect(status().isNotFound());

    verify(linkClickService, never()).recordClick(org.mockito.ArgumentMatchers.anyString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"short", "waytoolongofanalias"})
  void syntacticallyInvalidAliasLengthReturns404(String alias) throws Exception {
    when(linkLookupService.resolve(alias)).thenThrow(new AliasNotFoundException(alias));

    mockMvc.perform(get("/" + alias)).andExpect(status().isNotFound());
  }

  @Test
  void repositoryFailureReturns503NotAFalse404() throws Exception {
    when(linkLookupService.resolve("abc12345"))
        .thenThrow(new StorageUnavailableException("Unable to resolve alias: storage failure"));

    mockMvc.perform(get("/abc12345")).andExpect(status().isServiceUnavailable());
  }

  @Test
  void apiRoutesAreNotSwallowedByTheAliasCatchAll() throws Exception {
    mockMvc.perform(get("/api/v1/links")).andExpect(status().isNotFound());

    verifyNoInteractions(linkLookupService);
  }
}

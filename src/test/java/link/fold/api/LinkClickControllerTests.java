package link.fold.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import link.fold.domain.AliasNotFoundException;
import link.fold.domain.LinkClickService;
import link.fold.domain.StorageUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MVC tests for {@code GET /api/v1/links/{alias}/clicks}: the bare-number success response, the
 * missing/invalid-alias 404 path, and the storage-failure 503 path.
 */
@WebMvcTest(controllers = LinkClickController.class)
class LinkClickControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LinkClickService linkClickService;

  @Test
  void returnsTheClickCountAsABareNumber() throws Exception {
    when(linkClickService.countClicks("abc12345")).thenReturn(42L);

    mockMvc
        .perform(get("/api/v1/links/abc12345/clicks"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string("42"));
  }

  @Test
  void returnsZeroForAnAliasThatHasNeverBeenClicked() throws Exception {
    when(linkClickService.countClicks("abc12345")).thenReturn(0L);

    mockMvc
        .perform(get("/api/v1/links/abc12345/clicks"))
        .andExpect(status().isOk())
        .andExpect(content().string("0"));
  }

  @Test
  void unknownAliasReturns404() throws Exception {
    when(linkClickService.countClicks("unknown1"))
        .thenThrow(new AliasNotFoundException("unknown1"));

    mockMvc.perform(get("/api/v1/links/unknown1/clicks")).andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {"short", "waytoolongofanalias"})
  void syntacticallyInvalidAliasLengthReturns404(String alias) throws Exception {
    when(linkClickService.countClicks(alias)).thenThrow(new AliasNotFoundException(alias));

    mockMvc.perform(get("/api/v1/links/" + alias + "/clicks")).andExpect(status().isNotFound());
  }

  @Test
  void repositoryFailureReturns503() throws Exception {
    when(linkClickService.countClicks("abc12345"))
        .thenThrow(new StorageUnavailableException("Unable to count clicks: storage failure"));

    mockMvc
        .perform(get("/api/v1/links/abc12345/clicks"))
        .andExpect(status().isServiceUnavailable());
  }
}

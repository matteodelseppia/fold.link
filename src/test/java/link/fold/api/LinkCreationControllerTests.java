package link.fold.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import link.fold.config.AppProperties;
import link.fold.domain.AliasNotFoundException;
import link.fold.domain.InvalidDestinationException;
import link.fold.domain.LinkCreationService;
import link.fold.domain.LinkMapping;
import link.fold.domain.StorageUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MVC tests for {@code POST /api/v1/links}: success shape/headers/service-argument passthrough,
 * spoofed-forwarded-host resistance, and every validation/failure mapping owned by {@link
 * ApiExceptionHandler}.
 */
@WebMvcTest(controllers = LinkCreationController.class)
class LinkCreationControllerTests {

  @TestConfiguration
  static class Config {
    @Bean
    AppProperties appProperties() {
      return new AppProperties(
          "https://fold.link", new AppProperties.Alias(8, 5), new AppProperties.Redis("v1:link:"));
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LinkCreationService linkCreationService;

  @Test
  void createsALinkAndReturnsExactSuccessJson() throws Exception {
    when(linkCreationService.createLink("https://example.com/page"))
        .thenReturn(new LinkMapping("abc12345", "https://example.com/page"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com/page\"}"))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.alias").value("abc12345"))
        .andExpect(jsonPath("$.shortUrl").value("https://fold.link/abc12345"))
        .andExpect(jsonPath("$.destination").value("https://example.com/page"));
  }

  @Test
  void spoofedForwardedHostNeverAffectsTheShortUrl() throws Exception {
    when(linkCreationService.createLink(anyString()))
        .thenReturn(new LinkMapping("abc12345", "https://example.com"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .header("X-Forwarded-Host", "evil.example")
                .header("Host", "evil.example")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\"}"))
        .andExpect(jsonPath("$.shortUrl").value("https://fold.link/abc12345"));
  }

  @Test
  void passesTheRawRequestUrlThroughToTheService() throws Exception {
    when(linkCreationService.createLink(anyString()))
        .thenReturn(new LinkMapping("abc12345", "https://example.com"));

    mockMvc.perform(
        post("/api/v1/links")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"https://example.com\"}"));

    verify(linkCreationService).createLink("https://example.com");
  }

  @Test
  void invalidDestinationReturns400ValidationError() throws Exception {
    when(linkCreationService.createLink(anyString()))
        .thenThrow(new InvalidDestinationException("url must use the http or https scheme"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"javascript:alert(1)\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void emptyBodyReturns400ValidationError() throws Exception {
    mockMvc
        .perform(post("/api/v1/links").contentType(MediaType.APPLICATION_JSON).content(""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void malformedJsonReturns400ValidationError() throws Exception {
    mockMvc
        .perform(post("/api/v1/links").contentType(MediaType.APPLICATION_JSON).content("{not-json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void missingUrlFieldReturns400ValidationError() throws Exception {
    mockMvc
        .perform(post("/api/v1/links").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void blankUrlReturns400ValidationError() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"   \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void unsupportedContentTypeReturns415() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/links").contentType(MediaType.TEXT_PLAIN).content("https://example.com"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  void exactBoundaryUrlLengthIsAccepted() throws Exception {
    String url = "https://example.com/" + "a".repeat(2048 - "https://example.com/".length());
    when(linkCreationService.createLink(url)).thenReturn(new LinkMapping("abc12345", url));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + url + "\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void overBoundaryUrlLengthReturns400() throws Exception {
    String url = "https://example.com/" + "a".repeat(2049 - "https://example.com/".length());

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + url + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void collisionExhaustionReturns503() throws Exception {
    when(linkCreationService.createLink(anyString()))
        .thenThrow(
            new StorageUnavailableException("Unable to store link: alias retries exhausted"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\"}"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("STORAGE_ERROR"));
  }

  @Test
  void repositoryFailureReturns503() throws Exception {
    when(linkCreationService.createLink(anyString()))
        .thenThrow(new StorageUnavailableException("Unable to store link: storage failure"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\"}"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void unexpectedExceptionFallsBackTo500() throws Exception {
    when(linkCreationService.createLink(anyString())).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }

  @Test
  void aliasNotFoundExceptionIsStillMappedTo404ByTheSharedHandler() throws Exception {
    // Defensive: this controller never itself surfaces AliasNotFoundException, but the shared
    // ApiExceptionHandler must still map it correctly if some future creation path does.
    when(linkCreationService.createLink(anyString())).thenThrow(new AliasNotFoundException("x"));

    mockMvc
        .perform(
            post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\"}"))
        .andExpect(status().isNotFound());
  }
}

package link.fold.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import link.fold.config.AppProperties;
import link.fold.domain.AliasNotFoundException;
import link.fold.domain.LinkLookupService;
import link.fold.domain.QrCodeService;
import link.fold.domain.QrGenerationException;
import link.fold.domain.StorageUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MVC tests for {@code GET /api/v1/links/{alias}/qr}: the PNG success response built from the
 * configured public origin (never a request header), and every validation/failure mapping owned by
 * {@link ApiExceptionHandler}.
 */
@WebMvcTest(controllers = LinkQrController.class)
class LinkQrControllerTests {

  @TestConfiguration
  static class Config {
    @Bean
    AppProperties appProperties() {
      return new AppProperties(
          "https://fold.link",
          new AppProperties.Alias(8, 5),
          new AppProperties.Redis("v1:link:", Duration.ofDays(3)));
    }
  }

  private static final byte[] FAKE_PNG = {1, 2, 3, 4};

  @Autowired private MockMvc mockMvc;

  @MockitoBean private LinkLookupService linkLookupService;

  @MockitoBean private QrCodeService qrCodeService;

  @Test
  void returnsThePngBytesFromTheQrCodeServiceForTheAliasShortUrl() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/page");
    when(qrCodeService.generatePng("https://fold.link/abc12345")).thenReturn(FAKE_PNG);

    mockMvc
        .perform(get("/api/v1/links/abc12345/qr"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(FAKE_PNG));

    verify(qrCodeService).generatePng("https://fold.link/abc12345");
  }

  @Test
  void resolvesTheAliasBeforeGeneratingTheCode() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/page");
    when(qrCodeService.generatePng(anyString())).thenReturn(FAKE_PNG);

    mockMvc.perform(get("/api/v1/links/abc12345/qr"));

    verify(linkLookupService).resolve("abc12345");
  }

  @Test
  void responseIsCacheableForAWhileButMustRevalidate() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/page");
    when(qrCodeService.generatePng(anyString())).thenReturn(FAKE_PNG);

    mockMvc
        .perform(get("/api/v1/links/abc12345/qr"))
        .andExpect(header().string("Cache-Control", "max-age=3600, must-revalidate"));
  }

  @Test
  void spoofedForwardedHostNeverAffectsTheEncodedShortUrl() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/page");
    when(qrCodeService.generatePng(anyString())).thenReturn(FAKE_PNG);

    mockMvc.perform(
        get("/api/v1/links/abc12345/qr")
            .header("X-Forwarded-Host", "evil.example")
            .header("Host", "evil.example"));

    verify(qrCodeService).generatePng("https://fold.link/abc12345");
  }

  @Test
  void unknownAliasReturns404AndNeverGeneratesACode() throws Exception {
    when(linkLookupService.resolve("unknown1")).thenThrow(new AliasNotFoundException("unknown1"));

    mockMvc
        .perform(get("/api/v1/links/unknown1/qr"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("ALIAS_NOT_FOUND"));

    verifyNoInteractions(qrCodeService);
  }

  @ParameterizedTest
  @ValueSource(strings = {"short", "waytoolongofanalias"})
  void syntacticallyInvalidAliasLengthReturns404(String alias) throws Exception {
    when(linkLookupService.resolve(alias)).thenThrow(new AliasNotFoundException(alias));

    mockMvc.perform(get("/api/v1/links/" + alias + "/qr")).andExpect(status().isNotFound());

    verifyNoInteractions(qrCodeService);
  }

  @Test
  void lookupStorageFailureReturns503AndNeverGeneratesACode() throws Exception {
    when(linkLookupService.resolve("abc12345"))
        .thenThrow(new StorageUnavailableException("Unable to resolve alias: storage failure"));

    mockMvc
        .perform(get("/api/v1/links/abc12345/qr"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.error").value("STORAGE_ERROR"));

    verifyNoInteractions(qrCodeService);
  }

  @Test
  void qrGenerationFailureReturns500WithTheQrErrorCode() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenReturn("https://example.com/page");
    when(qrCodeService.generatePng(anyString()))
        .thenThrow(new QrGenerationException("Unable to encode content as a QR code", null));

    mockMvc
        .perform(get("/api/v1/links/abc12345/qr"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("QR_GENERATION_ERROR"));
  }

  @Test
  void unexpectedExceptionFallsBackTo500() throws Exception {
    when(linkLookupService.resolve("abc12345")).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(get("/api/v1/links/abc12345/qr"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"));
  }
}

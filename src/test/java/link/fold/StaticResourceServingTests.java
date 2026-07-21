package link.fold;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the static frontend (src/main/resources/static/) is served correctly and does not
 * collide with the API/redirect routes: {@code /} returns the page, assets return the right media
 * type and a revalidating cache policy, and {@code GET /{alias}} still owns single-segment paths
 * rather than being swallowed by the static resource handler.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StaticResourceServingTests {

  @LocalServerPort private int port;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void rootServesTheFrontendPage() throws Exception {
    HttpResponse<String> response = get("/");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Content-Type"))
        .get()
        .asString()
        .contains("text/html");
    assertThat(response.body()).contains("<title>foldl.ink").contains("id=\"shorten-form\"");
  }

  @Test
  void rootPageFooterNotesTheLinkExpiryWindow() throws Exception {
    HttpResponse<String> response = get("/");

    assertThat(response.body()).containsIgnoringCase("expire in 3 days");
  }

  @Test
  void rootResponseHasARevalidatingCachePolicy() throws Exception {
    HttpResponse<String> response = get("/");

    assertThat(response.headers().firstValue("Cache-Control"))
        .get()
        .asString()
        .contains("must-revalidate");
  }

  @Test
  void cssAssetIsServedWithTheCorrectMediaType() throws Exception {
    HttpResponse<String> response = get("/css/styles.css");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Content-Type")).get().asString().contains("text/css");
    assertThat(response.body()).contains("body");
  }

  @Test
  void jsAssetIsServedWithAJavaScriptMediaType() throws Exception {
    HttpResponse<String> response = get("/js/app.js");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.headers().firstValue("Content-Type"))
        .get()
        .asString()
        .containsAnyOf("javascript", "ecmascript");
    assertThat(response.body()).contains("shorten-form");
  }

  @Test
  void aliasRoutingStillOwnsSingleSegmentPathsOverStaticHandling() throws Exception {
    // A syntactically-invalid-shape alias must still 404 from the redirect controller (not from
    // the static handler, and not a 500) - proving /{alias} takes precedence for non-asset paths.
    HttpResponse<String> response = get("/not-a-real-alias-xyz");

    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  void unmappedApiStylePathIsNotSwallowedByTheStaticCatchAll() throws Exception {
    HttpResponse<String> response = get("/api/does-not-exist");

    assertThat(response.statusCode()).isEqualTo(404);
  }
}

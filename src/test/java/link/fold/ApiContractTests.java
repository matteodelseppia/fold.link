package link.fold;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Black-box HTTP contract tests for the link-creation and redirect APIs, run against the full
 * packaged application (random local port) and a real, reachable Redis - skipped, not failed, if
 * none is reachable, matching {@code link.fold.health}'s convention. Each requirement from
 * requirements.md that these endpoints satisfy (F01, F02, F03, F05) is named directly in a test.
 *
 * <p>Uses only public HTTP behavior and the exact schema from ADR-001 - no internal types.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiContractTests {

  @DynamicPropertySource
  static void uniqueKeyPrefix(DynamicPropertyRegistry registry) {
    registry.add("app.redis.key-prefix", () -> "contract-test:" + UUID.randomUUID() + ":");
  }

  @BeforeAll
  static void assumeRedisIsReachable() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 6379), 500);
    } catch (IOException e) {
      assumeTrue(
          false,
          "Redis is not reachable on localhost:6379 - skipping (run scripts/dev/redis-start.sh first)");
    }
  }

  @LocalServerPort private int port;

  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private HttpResponse<String> createLink(String url) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(baseUrl() + "/api/v1/links"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"url\":\"" + url + "\"}"))
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> getAlias(String alias) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl() + "/" + alias)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private String extractJsonField(String json, String field) {
    var matcher = java.util.regex.Pattern.compile("\"" + field + "\":\"([^\"]*)\"").matcher(json);
    if (!matcher.find()) {
      throw new AssertionError("field " + field + " not found in " + json);
    }
    return matcher.group(1);
  }

  @Test
  void f01_createsAUniqueShortenedAliasForAValidLongUrl() throws Exception {
    String destination = "https://example.com/f01/" + UUID.randomUUID();

    HttpResponse<String> response = createLink(destination);

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.headers().firstValue("Content-Type"))
        .get()
        .asString()
        .contains("application/json");
    String alias = extractJsonField(response.body(), "alias");
    assertThat(alias).matches("[A-Za-z0-9_-]{8}");
    assertThat(extractJsonField(response.body(), "destination")).isEqualTo(destination);
    // app.base-url (application-test.yml) is fixed, independent of this test server's actual
    // random port - the short URL is always built from configured origin, never request state.
    assertThat(extractJsonField(response.body(), "shortUrl"))
        .isEqualTo("http://localhost:8080/" + alias);
  }

  @Test
  void f02_redirectsToTheOriginalLongUrlForAValidShortenedUrl() throws Exception {
    String destination = "https://example.com/f02/" + UUID.randomUUID();
    String alias = extractJsonField(createLink(destination).body(), "alias");

    HttpResponse<String> response = getAlias(alias);

    assertThat(response.statusCode()).isEqualTo(302);
    assertThat(response.headers().firstValue("Location")).contains(destination);
  }

  @Test
  void f03_returns404ForAShortenedUrlThatDoesNotExist() throws Exception {
    HttpResponse<String> response = getAlias("zzzzzzzz");

    assertThat(response.statusCode()).isEqualTo(404);
    assertThat(extractJsonField(response.body(), "error")).isEqualTo("ALIAS_NOT_FOUND");
  }

  @Test
  void f05_rejectsAMalformedUrlWithAClearValidationError() throws Exception {
    HttpResponse<String> response = createLink("not-a-valid-url");

    assertThat(response.statusCode()).isEqualTo(400);
    assertThat(extractJsonField(response.body(), "error")).isEqualTo("VALIDATION_ERROR");
    assertThat(extractJsonField(response.body(), "message")).isNotBlank();
  }
}

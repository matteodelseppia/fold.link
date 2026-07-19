package link.fold.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Exercises the health/liveness/readiness endpoints against a real, reachable Redis
 * (application-test.yml's localhost:6379 default — the Compose service from MVP-016, or the "redis"
 * CI service in .gitlab-ci.yml's unit_test job). If no Redis is reachable (e.g. a local `./gradlew
 * test` run without `scripts/dev/redis-start.sh`), these tests are skipped rather than failed —
 * {@link ReadinessFailsWhenRedisUnavailableTests} already covers the no-Redis path unconditionally.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthyResponseWhenRedisAvailableTests {

  @BeforeAll
  static void assumeRedisIsReachable() {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 6379), 500);
    } catch (IOException e) {
      assumeTrue(
          false,
          "Redis is not reachable on localhost:6379 — skipping (run scripts/dev/redis-start.sh first)");
    }
  }

  @LocalServerPort private int port;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void aggregateHealthIsUp() throws Exception {
    HttpResponse<String> response = get("/actuator/health");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"status\":\"UP\"");
  }

  @Test
  void livenessIsUp() throws Exception {
    HttpResponse<String> response = get("/actuator/health/liveness");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"status\":\"UP\"");
  }

  @Test
  void readinessIsUp() throws Exception {
    HttpResponse<String> response = get("/actuator/health/readiness");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"status\":\"UP\"");
  }
}

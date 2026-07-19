package link.fold.health;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Redis pointed at an address nothing listens on. Liveness must stay UP regardless (it never
 * depends on Redis); readiness must report DOWN, since the app can't actually serve create/redirect
 * traffic without it.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReadinessFailsWhenRedisUnavailableTests {

  @DynamicPropertySource
  static void unreachableRedis(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", () -> "127.0.0.1");
    registry.add("spring.data.redis.port", () -> "19999");
  }

  @LocalServerPort private int port;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void livenessStaysUpWhenRedisIsUnreachable() throws Exception {
    HttpResponse<String> response = get("/actuator/health/liveness");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"status\":\"UP\"");
  }

  @Test
  void readinessReportsDownWhenRedisIsUnreachable() throws Exception {
    HttpResponse<String> response = get("/actuator/health/readiness");

    assertThat(response.statusCode()).isEqualTo(503);
    assertThat(response.body()).contains("\"status\":\"DOWN\"");
  }
}

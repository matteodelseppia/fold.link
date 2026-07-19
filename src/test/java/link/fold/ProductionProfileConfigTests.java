package link.fold;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The production profile requires REDIS_HOST/APP_BASE_URL to come from the environment; it binds to
 * whatever fixture is supplied here and never to a value shared with the staging test, proving the
 * profiles can't cross over.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("production")
class ProductionProfileConfigTests {

  @DynamicPropertySource
  static void fixtureVariables(DynamicPropertyRegistry registry) {
    registry.add("REDIS_HOST", () -> "fixture-production-redis.internal");
    registry.add("APP_BASE_URL", () -> "https://fixture.invalid");
  }

  @Autowired private Environment env;

  @Test
  void bindsUsingFixtureEnvironmentVariables() {
    assertThat(env.getProperty("spring.data.redis.host"))
        .isEqualTo("fixture-production-redis.internal");
    assertThat(env.getProperty("app.base-url")).isEqualTo("https://fixture.invalid");
  }
}

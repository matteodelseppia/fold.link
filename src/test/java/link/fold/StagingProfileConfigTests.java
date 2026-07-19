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
 * The staging profile requires REDIS_HOST/APP_BASE_URL to come from the environment; no hosted
 * endpoint is committed.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("staging")
class StagingProfileConfigTests {

  @DynamicPropertySource
  static void fixtureVariables(DynamicPropertyRegistry registry) {
    registry.add("REDIS_HOST", () -> "fixture-staging-redis.internal");
    registry.add("APP_BASE_URL", () -> "https://staging.fixture.invalid");
  }

  @Autowired private Environment env;

  @Test
  void bindsUsingFixtureEnvironmentVariables() {
    assertThat(env.getProperty("spring.data.redis.host"))
        .isEqualTo("fixture-staging-redis.internal");
    assertThat(env.getProperty("app.base-url")).isEqualTo("https://staging.fixture.invalid");
  }
}

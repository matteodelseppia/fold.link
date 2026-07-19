package link.fold;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * application-test.yml lives under src/test/resources only, so it is never
 * packaged into the application artifact and can never be activated against
 * a real deployment.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class TestProfileConfigTests {

    @Autowired
    private Environment env;

    @Test
    void bindsWithFixturesIsolatedFromMainResources() {
        assertThat(env.getProperty("spring.data.redis.host")).isEqualTo("localhost");
        assertThat(env.getProperty("app.base-url")).isEqualTo("http://localhost:8080");
    }
}

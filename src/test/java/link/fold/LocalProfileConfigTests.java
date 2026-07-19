package link.fold;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/** The local profile ships safe defaults, so no fixture variables are needed for binding to succeed. */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("local")
class LocalProfileConfigTests {

    @Autowired
    private Environment env;

    @Test
    void bindsWithoutRequiringExternalFixtures() {
        assertThat(env.getProperty("spring.data.redis.host")).isEqualTo("localhost");
        assertThat(env.getProperty("app.base-url")).isEqualTo("http://localhost:8080");
    }
}

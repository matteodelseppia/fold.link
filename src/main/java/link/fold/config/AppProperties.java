package link.fold.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Runtime configuration for the fold.link application, bound and validated
 * from {@code app.*} properties. Defaults for alias length and the Redis
 * key prefix mirror ADR-001 (docs/milestones/mvp/adr-001-mvp-technical-decisions.md).
 *
 * <p>{@code baseUrl} has no default: outside tests it must come from the
 * environment ({@code APP_BASE_URL}), and startup fails with a clear
 * validation error if it is missing or not a well-formed URL.
 */
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @NotBlank @URL String baseUrl,
        @Valid Alias alias,
        @Valid Redis redis) {

    public record Alias(
            @Min(6) @Max(32) int length,
            @Min(1) @Max(20) int retryCount) {

        public Alias(@DefaultValue("8") int length, @DefaultValue("5") int retryCount) {
            this.length = length;
            this.retryCount = retryCount;
        }
    }

    public record Redis(@NotBlank String keyPrefix) {

        public Redis(@DefaultValue("v1:link:") String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }
}

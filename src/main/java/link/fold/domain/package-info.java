/**
 * Framework-independent domain model and application services for URL shortening: destination
 * validation/canonicalization, alias generation, the {@link link.fold.domain.UrlMappingRepository}
 * port, and the link-creation/lookup services that use them. No Spring MVC or Redis types appear
 * here (see {@code link.fold.api} and {@code link.fold.redis}).
 */
package link.fold.domain;

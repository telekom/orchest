package io.telekom.orchest.orchestrest.configurations.security;

import static io.telekom.orchest.orchestrest.configurations.security.SecurityConfiguration.AUTH_WHITELIST;

import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.service.UserApiTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** WebFilter that authenticates requests via the X-API-Key header using stored API tokens. */
@Slf4j
public class ApiKeyAuthWebFilter implements WebFilter {

  public static final String PRE_AUTHENTICATED_ATTR = "ORCHEST_PRE_AUTHENTICATED";
  public static final String LOGGED_IN_USER_CONTEXT_ATTR = "ORCHEST_LOGGED_IN_USER_CONTEXT";
  private static final String API_KEY_HEADER = "X-API-Key";

  private final UserApiTokenService tokenService;
  private final ApiTokenProperties properties;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public ApiKeyAuthWebFilter(UserApiTokenService tokenService, ApiTokenProperties properties) {
    this.tokenService = tokenService;
    this.properties = properties;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (!properties.isEnabled()) {
      return chain.filter(exchange);
    }

    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    if (isWhitelisted(path)) {
      return chain.filter(exchange);
    }

    String apiKey = request.getHeaders().getFirst(API_KEY_HEADER);
    if (apiKey == null || apiKey.isBlank()) {
      return chain.filter(exchange);
    }

    return Mono.fromCallable(() -> tokenService.validateToken(apiKey))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            optionalToken -> {
              if (optionalToken.isEmpty()) {
                return unauthorized(exchange, "Invalid or expired API key");
              }

              UserApiToken token = optionalToken.get();
              boolean isAdmin = token.getRoles().contains(AuthWebFilter.ADMIN);

              LoggedInUserContext ctx =
                  LoggedInUserContext.builder()
                      .userId(token.getUserId())
                      .roles(token.getRoles())
                      .primaryRole(determinePrimaryRole(token.getRoles()))
                      .admin(isAdmin)
                      .build();

              UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                      token.getUserId(),
                      null,
                      token.getRoles().stream().map(SimpleGrantedAuthority::new).toList());

              exchange.getAttributes().put(PRE_AUTHENTICATED_ATTR, Boolean.TRUE);
              exchange.getAttributes().put(LOGGED_IN_USER_CONTEXT_ATTR, ctx);

              Mono.fromRunnable(() -> tokenService.updateLastUsed(token.getId()))
                  .subscribeOn(Schedulers.boundedElastic())
                  .subscribe();

              return chain
                  .filter(exchange)
                  .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                  .contextWrite(
                      reactor.util.context.Context.of(LoggedInUserContext.CONTEXT_KEY, ctx));
            });
  }

  private static String determinePrimaryRole(java.util.List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return null;
    }
    if (roles.size() == 1) {
      return roles.getFirst();
    }
    if (roles.contains(AuthWebFilter.ADMIN)) {
      return AuthWebFilter.ADMIN;
    }
    if (roles.contains(AuthWebFilter.READ_USER)) {
      return AuthWebFilter.READ_USER;
    }
    if (roles.contains(AuthWebFilter.READ_USER_NON_SENSITIVE)) {
      return AuthWebFilter.READ_USER_NON_SENSITIVE;
    }
    return roles.getFirst();
  }

  private boolean isWhitelisted(String path) {
    for (String pattern : AUTH_WHITELIST) {
      if (pathMatcher.match("/orchest" + pattern, path) || pathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    return SecurityResponseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, message);
  }
}

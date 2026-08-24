package io.telekom.orchest.orchestrest.configurations.security;

import static io.telekom.orchest.orchestrest.configurations.security.SecurityConfiguration.AUTH_WHITELIST;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive WebFilter that extracts user identity from JWT tokens and stores the {@link
 * LoggedInUserContext} in the Reactor Context for downstream consumption.
 *
 * <p>Replaces the servlet-based OncePerRequestFilter. Whitelisted paths are skipped;
 * non-whitelisted paths are validated when security is enabled.
 */
@Slf4j
@Order(-100)
@Component
public class AuthWebFilter implements WebFilter {

  public static final String ADMIN = "ORCHEST_ADMIN";
  public static final String READ_USER = "ORCHEST_READ_ONLY";
  public static final String READ_USER_NON_SENSITIVE = "ORCHEST_READ_NON_SENSITIVE";

  private static final List<String> ALL_USERS =
      Arrays.asList(READ_USER, READ_USER_NON_SENSITIVE, ADMIN);
  private static final List<String> ADMIN_ACCESSIBLE_PATH = new ArrayList<>();

  static {
    // Variables
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/variables");
    ADMIN_ACCESSIBLE_PATH.add("GET_/orchest/variables");

    // Process Definitions
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processDefinitions/upload");
    ADMIN_ACCESSIBLE_PATH.add("DELETE_/orchest/processDefinitions/**");

    // Process Instances
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processInstances/create");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processInstances/retry");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processInstances/retryBatch");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processInstances/resolveIncident");
    ADMIN_ACCESSIBLE_PATH.add("PATCH_/orchest/processInstances/modifyInstance");
    ADMIN_ACCESSIBLE_PATH.add("PATCH_/orchest/processInstances/cancelInstance");
    ADMIN_ACCESSIBLE_PATH.add("PATCH_/orchest/processInstances/cancelInstances");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processInstances/cancelBatch");

    // Dynamic Process Instances
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/dyanmicProcessInstances/create");

    // Decision Definitions
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/decisionDefinitions/upload");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/decisionDefinitions/evaluate");
    ADMIN_ACCESSIBLE_PATH.add("DELETE_/orchest/decisionDefinitions/**");

    // Rate Limit
    ADMIN_ACCESSIBLE_PATH.add("PUT_/orchest/rateLimit");

    // Eventing
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/eventing/sendMessage");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/eventing/sendSignal");

    // Incidents
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/incidents/resolve");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/incidents/resolve/**");

    // User Tasks
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/userTasks/*/claim");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/userTasks/*/unclaim");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/userTasks/*/complete");
    ADMIN_ACCESSIBLE_PATH.add("PATCH_/orchest/userTasks/*/assign");

    // Connectors
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/connectors/upload");

    // Feel Playground
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/feelPlayground/evaluate");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/feelPlayground/validate");

    // Chat (AI)
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/chats/analyse");

    // Deployment Approvals
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/deploymentApprovals/requestApproval");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/deploymentApprovals/submitApproval");
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/deploymentApprovals/addApprovers");
    ADMIN_ACCESSIBLE_PATH.add("DELETE_/orchest/deploymentApprovals/removeApprover");

    // Process Definition Sensitive Variables
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processDefinitionsSensitiveVariables");
    ADMIN_ACCESSIBLE_PATH.add("PATCH_/orchest/processDefinitionsSensitiveVariables");
    ADMIN_ACCESSIBLE_PATH.add("DELETE_/orchest/processDefinitionsSensitiveVariables");

    // Process Definition Environments
    ADMIN_ACCESSIBLE_PATH.add("POST_/orchest/processDefinitionsEnvs");
    ADMIN_ACCESSIBLE_PATH.add("PATCH_/orchest/processDefinitionsEnvs");
    ADMIN_ACCESSIBLE_PATH.add("DELETE_/orchest/processDefinitionsEnvs");

    // Audit Trail
    ADMIN_ACCESSIBLE_PATH.add("GET_/orchest/auditTrails/**");
  }

  private final Map<String, JwtSecurityProperties.AudienceConfig> audienceConfigMap;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Value("${enableSecurity:false}")
  private boolean enableSecurity;

  @Value("${spring.profiles.active:local}")
  private String profile;

  public AuthWebFilter(JwtSecurityProperties jwtSecurityProperties) {
    if (jwtSecurityProperties != null && !jwtSecurityProperties.getIssuers().isEmpty()) {
      this.audienceConfigMap =
          jwtSecurityProperties.getAllAudiences().stream()
              .collect(
                  Collectors.toMap(
                      JwtSecurityProperties.AudienceConfig::getAud, Function.identity()));
      log.info(
          "AuthWebFilter initialized with {} audience configurations", audienceConfigMap.size());
    } else {
      this.audienceConfigMap = Map.of();
      log.warn("AuthWebFilter initialized with no audience configurations");
    }
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (Boolean.TRUE.equals(exchange.getAttribute(ApiKeyAuthWebFilter.PRE_AUTHENTICATED_ATTR))) {
      LoggedInUserContext ctx =
          exchange.getAttribute(ApiKeyAuthWebFilter.LOGGED_IN_USER_CONTEXT_ATTR);
      if (ctx != null) {
        return chain
            .filter(exchange)
            .contextWrite(reactor.util.context.Context.of(LoggedInUserContext.CONTEXT_KEY, ctx));
      }
      return chain.filter(exchange);
    }

    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();
    String method = request.getMethod().name();

    if (isWhitelisted(path)) {
      LoggedInUserContext ctx = buildAnonymousContext();
      return chain
          .filter(exchange)
          .contextWrite(reactor.util.context.Context.of(LoggedInUserContext.CONTEXT_KEY, ctx));
    }

    if (profile.equalsIgnoreCase("local") || profile.equalsIgnoreCase("dev")) {
      LoggedInUserContext ctx = buildLocalDevContext();
      return chain
          .filter(exchange)
          .contextWrite(reactor.util.context.Context.of(LoggedInUserContext.CONTEXT_KEY, ctx));
    }

    if (!enableSecurity) {
      LoggedInUserContext ctx = buildLocalDevContext();
      return chain
          .filter(exchange)
          .contextWrite(reactor.util.context.Context.of(LoggedInUserContext.CONTEXT_KEY, ctx));
    }

    // Security is enabled — validate the bearer token
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return unauthorized(exchange);
    }

    try {
      String token = authHeader.substring(7);
      String payloadJson = AuthWebFilter.decodeTokenPayload(token);
      Map<String, Object> payload = JsonMapper.readFromJson(payloadJson, new TypeReference<>() {});

      String loggedInUser = payload.get("preferred_username").toString();
      List<String> roles =
          JsonMapper.readFromJson(JsonMapper.writeToJson(payload.get("roles")), List.class);

      if (roles == null || roles.isEmpty()) {
        return forbidden(exchange);
      }

      roles = mapRoles(payload, roles);

      boolean isAdmin = roles.contains(ADMIN);
      String permissionKey = method + "_" + path;

      if (!isAdmin && isAdminOnlyPath(permissionKey)) {
        return forbidden(exchange);
      }
      if (!isAdmin && ALL_USERS.stream().noneMatch(roles::contains)) {
        return forbidden(exchange);
      }

      LoggedInUserContext ctx =
          LoggedInUserContext.builder()
              .userId(loggedInUser)
              .roles(roles)
              .primaryRole(determinePrimaryRole(roles))
              .admin(isAdmin)
              .build();

      return chain
          .filter(exchange)
          .contextWrite(reactor.util.context.Context.of(LoggedInUserContext.CONTEXT_KEY, ctx));

    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Auth filter rejected request: {}", e.getMessage());
      return unauthorized(exchange);
    }
  }

  private boolean isWhitelisted(String path) {
    for (String pattern : AUTH_WHITELIST) {
      if (pathMatcher.match("/orchest" + pattern, path) || pathMatcher.match(pattern, path)) {
        return true;
      }
    }
    return false;
  }

  private boolean isAdminOnlyPath(String permissionKey) {
    if (ADMIN_ACCESSIBLE_PATH.contains(permissionKey)) {
      return true;
    }
    return ADMIN_ACCESSIBLE_PATH.stream().anyMatch(p -> pathMatcher.match(p, permissionKey));
  }

  private LoggedInUserContext buildLocalDevContext() {
    return LoggedInUserContext.builder()
        .userId("dev@example.com")
        .roles(List.of(ADMIN))
        .primaryRole(ADMIN)
        .admin(true)
        .build();
  }

  private LoggedInUserContext buildAnonymousContext() {
    return LoggedInUserContext.builder().roles(List.of()).admin(false).build();
  }

  public static String decodeTokenPayload(String token) {
    org.apache.commons.codec.binary.Base64 base64 =
        new org.apache.commons.codec.binary.Base64(true);
    String[] chunks = token.split("\\.");
    return new String(base64.decode(chunks[1]));
  }

  private String determinePrimaryRole(List<String> roles) {
    if (roles.size() == 1) return roles.getFirst();
    if (roles.contains(ADMIN)) return ADMIN;
    if (roles.contains(READ_USER)) return READ_USER;
    if (roles.contains(READ_USER_NON_SENSITIVE)) return READ_USER_NON_SENSITIVE;
    return roles.getFirst();
  }

  private List<String> mapRoles(Map<String, Object> payload, List<String> originalRoles) {
    if (audienceConfigMap.isEmpty()) {
      return originalRoles;
    }
    String aud = extractAudience(payload);
    if (aud == null) {
      return originalRoles;
    }
    JwtSecurityProperties.AudienceConfig config = audienceConfigMap.get(aud);
    if (config == null || config.getRoleMapping() == null || config.getRoleMapping().isEmpty()) {
      return originalRoles;
    }
    Map<String, String> mapping = config.getRoleMapping();
    return originalRoles.stream().map(role -> mapping.getOrDefault(role, role)).toList();
  }

  @SuppressWarnings("unchecked")
  private String extractAudience(Map<String, Object> payload) {
    Object aud = payload.get("aud");
    if (aud instanceof String s) return s;
    if (aud instanceof List<?> list) return list.isEmpty() ? null : list.getFirst().toString();
    return aud != null ? aud.toString() : null;
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    return SecurityResponseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
  }

  private Mono<Void> forbidden(ServerWebExchange exchange) {
    return SecurityResponseWriter.writeError(
        exchange, HttpStatus.FORBIDDEN, "Permission denied to access resource");
  }
}

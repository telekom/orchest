package io.telekom.orchest.orchestrest.configurations;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable per-request user context propagated via Reactor Context. In WebFlux there is no
 * thread-local / request-scoped bean; instead this value object is written to the Reactor Context
 * by {@code AuthWebFilter} and read from it wherever the authenticated user's identity is required.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUserContext {

  public static final Object CONTEXT_KEY = LoggedInUserContext.class;

  private String userId;
  private List<String> roles;
  private String primaryRole;
  private boolean admin;
  private String correlationId;
}

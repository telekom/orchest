package io.telekom.orchest.rest.client;

import java.net.http.HttpRequest;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Injects authentication headers into every request made by the OrchesT client. Use with {@link
 * OrchesTRestClient#create(String, TokenAuthInjector)} so all APIs receive the same auth.
 */
@FunctionalInterface
public interface TokenAuthInjector {

  /**
   * Add auth headers to the request. Called once per request before it is sent.
   *
   * @param requestBuilder the request builder; add headers via {@link
   *     HttpRequest.Builder#header(String, String)} or {@link HttpRequest.Builder#setHeader(String,
   *     String)}
   */
  void inject(HttpRequest.Builder requestBuilder);

  /** Bearer token (Authorization: Bearer &lt;token&gt;) with a static value. */
  static TokenAuthInjector bearer(String token) {
    return bearer(() -> token);
  }

  /**
   * Bearer token (Authorization: Bearer &lt;token&gt;) with a supplier for refreshable tokens (e.g.
   * OAuth).
   */
  static TokenAuthInjector bearer(Supplier<String> tokenSupplier) {
    Objects.requireNonNull(tokenSupplier, "tokenSupplier");
    return requestBuilder -> {
      String token = tokenSupplier.get();
      if (token != null && !token.isEmpty()) {
        requestBuilder.header("Authorization", "Bearer " + token);
      }
    };
  }

  /** Custom header with a static value (e.g. X-Api-Key, X-Auth-Token). */
  static TokenAuthInjector header(String headerName, String value) {
    return header(headerName, () -> value);
  }

  /** Custom header with a supplier for dynamic values. */
  static TokenAuthInjector header(String headerName, Supplier<String> valueSupplier) {
    Objects.requireNonNull(headerName, "headerName");
    Objects.requireNonNull(valueSupplier, "valueSupplier");
    return requestBuilder -> {
      String value = valueSupplier.get();
      if (value != null && !value.isEmpty()) {
        requestBuilder.header(headerName, value);
      }
    };
  }

  /**
   * Returns a request interceptor suitable for {@link
   * io.telekom.orchest.client.invoker.ApiClient#setRequestInterceptor(Consumer)}.
   */
  default Consumer<HttpRequest.Builder> toRequestInterceptor() {
    TokenAuthInjector self = this;
    return self::inject;
  }
}

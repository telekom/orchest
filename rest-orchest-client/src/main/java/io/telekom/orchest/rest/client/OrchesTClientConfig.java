package io.telekom.orchest.rest.client;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for REST call timeouts used by {@link OrchesTRestClient}. Use {@link #builder()} to
 * create instances with custom values; unspecified values use defaults.
 */
public final class OrchesTClientConfig {

  private static final int DEFAULT_CONNECT_TIMEOUT_SEC = 10;
  private static final int DEFAULT_READ_TIMEOUT_SEC = 30;
  private static final int DEFAULT_WRITE_TIMEOUT_SEC = 30;

  private final long connectTimeoutMs;
  private final long readTimeoutMs;
  private final long writeTimeoutMs;

  private OrchesTClientConfig(Builder builder) {
    this.connectTimeoutMs = builder.connectTimeoutMs;
    this.readTimeoutMs = builder.readTimeoutMs;
    this.writeTimeoutMs = builder.writeTimeoutMs;
  }

  /** Default configuration: 10s connect, 30s read/write. */
  public static OrchesTClientConfig defaults() {
    return builder().build();
  }

  /** Creates a new builder for constructing a custom configuration. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the connect timeout in milliseconds.
   *
   * @return connect timeout in ms
   */
  public long getConnectTimeoutMs() {
    return connectTimeoutMs;
  }

  /**
   * Returns the read timeout in milliseconds.
   *
   * @return read timeout in ms
   */
  public long getReadTimeoutMs() {
    return readTimeoutMs;
  }

  /**
   * Reserved for callers that tune the HTTP stack; the generated {@code java.net.http} client does
   * not expose a separate write timeout.
   */
  public long getWriteTimeoutMs() {
    return writeTimeoutMs;
  }

  public static final class Builder {
    private long connectTimeoutMs = TimeUnit.SECONDS.toMillis(DEFAULT_CONNECT_TIMEOUT_SEC);
    private long readTimeoutMs = TimeUnit.SECONDS.toMillis(DEFAULT_READ_TIMEOUT_SEC);
    private long writeTimeoutMs = TimeUnit.SECONDS.toMillis(DEFAULT_WRITE_TIMEOUT_SEC);

    private Builder() {}

    /** Connect timeout (time to establish connection). Default 10 seconds. */
    public Builder connectTimeout(long duration, TimeUnit unit) {
      this.connectTimeoutMs = unit.toMillis(duration);
      return this;
    }

    /** Read timeout (time between each read from server). Default 30 seconds. */
    public Builder readTimeout(long duration, TimeUnit unit) {
      this.readTimeoutMs = unit.toMillis(duration);
      return this;
    }

    /** Write timeout (legacy; not applied by the built-in HTTP client). Default 30 seconds. */
    public Builder writeTimeout(long duration, TimeUnit unit) {
      this.writeTimeoutMs = unit.toMillis(duration);
      return this;
    }

    /**
     * Builds the configuration instance.
     *
     * @return the immutable configuration
     */
    public OrchesTClientConfig build() {
      return new OrchesTClientConfig(this);
    }
  }
}

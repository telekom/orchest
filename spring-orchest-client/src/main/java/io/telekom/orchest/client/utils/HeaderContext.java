package io.telekom.orchest.client.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Holder for storing header context data. Used to maintain request-specific headers across
 * different components.
 */
@Component
public class HeaderContext {

  private Map<String, Map<String, String>> headers;

  public HeaderContext() {
    this.headers = new ConcurrentHashMap<>();
  }

  /**
   * @return The entire map of headers.
   */
  public Map<String, Map<String, String>> get() {
    return this.headers;
  }

  /**
   * Retrieves headers for a specific key.
   *
   * @param key The key (e.g., request ID).
   * @return The map of headers associated with the key.
   */
  public Map<String, String> get(String key) {
    return this.headers.get(key);
  }

  /**
   * Sets the header map.
   *
   * @param headers The new header map.
   */
  public void set(Map<String, Map<String, String>> headers) {
    this.headers = headers;
  }

  /** Clears all stored headers. */
  public void destroy() {
    this.headers = new ConcurrentHashMap<>();
  }

  /**
   * Removes headers for a specific key.
   *
   * @param key The key to remove.
   */
  public void clear(String key) {
    if (this.headers != null) {
      this.headers.remove(key);
    }
  }
}

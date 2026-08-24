package io.telekom.orchest.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local context holder for storing execution context data. Used to pass contextual
 * information (e.g., trace IDs, user info) across the execution flow within the client.
 *
 * <p>Important: Call {@link #clear()} in a finally block after processing to prevent memory leaks
 * in thread pools.
 */
public class OrchestContextHolder {

  private static final ThreadLocal<Map<String, Object>> data =
      ThreadLocal.withInitial(HashMap::new);

  /**
   * Puts a key-value pair into the current thread's context.
   *
   * @param key The key.
   * @param value The value.
   */
  public static void put(String key, Object value) {
    data.get().put(key, value);
  }

  /**
   * Retrieves the entire context map for the current thread.
   *
   * @return The context map, never null.
   */
  public static Map<String, Object> get() {
    return data.get();
  }

  /**
   * Clears the context for the current thread. Should be called in a finally block after execution
   * is complete to prevent memory leaks.
   */
  public static void clear() {
    data.remove();
  }
}

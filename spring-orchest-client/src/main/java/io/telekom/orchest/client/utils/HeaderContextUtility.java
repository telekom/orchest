/* (C) 2022  Deutsche Telekom */
package io.telekom.orchest.client.utils;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility for managing header context. Provides methods to add and remove headers from the {@link
 * HeaderContext}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderContextUtility {

  private final HeaderContext headerContext;

  /**
   * Adds headers to the context, keyed by a value extracted from the headers map.
   *
   * @param headers The map of headers to store.
   * @param key The key in the map whose value will be used as the storage key (e.g., "requestId").
   */
  public synchronized void setHeaderContext(Map<String, String> headers, String key) {
    String requestId = headers.get(key);
    headerContext.get().put(requestId, headers);
    log.debug("header context added for requestId: {}", requestId);
  }

  /**
   * Removes headers from the context.
   *
   * @param headers The headers to remove (used to identify the key).
   * @param key The key in the map whose value identifies the entry to remove.
   */
  public synchronized void cleanHeaderContext(Map<String, String> headers, String key) {
    String requestId = headers.get(key);
    headerContext.get().remove(requestId);
    log.debug("header context cleaned for requestId: {}", requestId);
  }
}

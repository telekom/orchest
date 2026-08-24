package io.telekom.orchest.api.core.adapters.connector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Registry holding named connector executors for dispatching worker events. */
@Data
@NoArgsConstructor
public class ConnectorRegistry {

  /** Map of connector names to their executor implementations. */
  private final Map<String, ConnectorExecutor> connectorsExecutor = new ConcurrentHashMap<>();

  /**
   * Registers a connector executor under the given name.
   *
   * @param name the connector identifier
   * @param executor the executor implementation
   * @return the updated executor map
   */
  public Map<String, ConnectorExecutor> addExecutor(String name, ConnectorExecutor executor) {
    connectorsExecutor.put(name, executor);
    return connectorsExecutor;
  }
}

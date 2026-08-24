package io.telekom.orchest.api.core.adapters.connector;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ConnectorRegistry} verifying executor registration and retrieval. */
class ConnectorRegistryTest {

  @Test
  @DisplayName("addExecutor registers executor and returns the backing map")
  void addExecutor_registersAndReturnsMap() {
    ConnectorRegistry registry = new ConnectorRegistry();
    ConnectorExecutor exec = mock(ConnectorExecutor.class);

    var map = registry.addExecutor("io.orchest:http-json:1", exec);

    assertSame(exec, map.get("io.orchest:http-json:1"));
    assertSame(exec, registry.getConnectorsExecutor().get("io.orchest:http-json:1"));
  }

  @Test
  @DisplayName("multiple executors accumulate in registry")
  void addExecutor_multiple() {
    ConnectorRegistry registry = new ConnectorRegistry();
    ConnectorExecutor a = mock(ConnectorExecutor.class);
    ConnectorExecutor b = mock(ConnectorExecutor.class);

    registry.addExecutor("a", a);
    registry.addExecutor("b", b);

    assertSame(a, registry.getConnectorsExecutor().get("a"));
    assertSame(b, registry.getConnectorsExecutor().get("b"));
  }
}

package io.telekom.orchest.connectorservice.handler;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests JDBC connector input validation for query, URI, and detailed connection requirements. */
class JdbcConnectorHandlerTest {

  private JdbcConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() {
    handler = new JdbcConnectorHandler();
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "JDBC Task");
  }

  @Test
  void metadata() {
    assertEquals("io.orchest.connector-jdbc:1", handler.connectorType());
    assertEquals("JDBC_CONNECTOR_ERROR", handler.errorCode());
  }

  @Test
  void missingQuery_throws() {
    node.setInputMappings(List.of(new DataMapping("connection.uri", "jdbc:h2:mem:test")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("SQL query is required"));
  }

  @Test
  void missingUri_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("data.query", "SELECT 1"),
            new DataMapping("connection.authType", "uri")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("connection.uri is required"));
  }

  @Test
  void detailedConnection_requiresDatabaseAndHost() {
    node.setInputMappings(
        List.of(
            new DataMapping("data.query", "SELECT 1"),
            new DataMapping("connection.authType", "detailed")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("detailed JDBC connection"));
  }
}

package io.telekom.orchest.connectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.model.bpmn.Query;
import io.camunda.zeebe.model.bpmn.instance.ExtensionElements;
import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import java.util.Collections;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link BpmnConnectorParser} verifying connector registry lookups and property
 * population during parsing.
 */
@ExtendWith(MockitoExtension.class)
class BpmnConnectorParserTest {

  @Mock private FlowNode flowNode;

  @Mock private ExtensionElements extensionElements;

  @Test
  @DisplayName("registry recognizes bundled connector types")
  void isConnector() {
    assertTrue(BpmnConnectorParser.isConnector("io.orchest.http-json:1"));
    assertTrue(BpmnConnectorParser.isConnector("io.orchest.connector-microsoft-teams:1"));
    assertFalse(BpmnConnectorParser.isConnector("io.orchest.unknown:1"));
  }

  @Test
  @DisplayName("parseConnector sets connector flags and type on node")
  @SuppressWarnings("unchecked")
  void parseConnector_setsProperties() {
    Query<ModelElementInstance> query = mock(Query.class);
    when(flowNode.getExtensionElements()).thenReturn(extensionElements);
    when(extensionElements.getElementsQuery()).thenReturn(query);
    when(query.filterByType(any(Class.class))).thenReturn(query);
    when(query.list()).thenReturn(Collections.emptyList(), Collections.emptyList());

    ServiceTaskNode node = new ServiceTaskNode("n1", "task");

    BpmnConnectorParser.parseConnector("io.orchest.http-json:1", flowNode, node);

    assertTrue(Boolean.TRUE.equals(node.getProperties().get(BpmnConnectorParser.IS_CONNECTOR)));
    assertEquals(
        "io.orchest.http-json:1", node.getProperties().get(BpmnConnectorParser.CONNECTOR_TYPE));
  }
}

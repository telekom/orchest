package io.telekom.orchest.connectors.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.camunda.zeebe.model.bpmn.Query;
import io.camunda.zeebe.model.bpmn.instance.ExtensionElements;
import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import java.util.Collections;
import java.util.List;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link MSTeamConnectorParser} verifying I/O mapping extraction and retry configuration
 * handling from BPMN task definitions.
 */
@ExtendWith(MockitoExtension.class)
class MSTeamConnectorParserTest {

  @Mock private FlowNode flowNode;

  @Mock private ExtensionElements extensionElements;

  @Mock private ZeebeTaskDefinition taskDefinition;

  @Test
  @DisplayName("parse maps io and sets retries from first task definition when present")
  @SuppressWarnings("unchecked")
  void parse_withRetries() {
    Query<ModelElementInstance> query = mock(Query.class);
    when(flowNode.getExtensionElements()).thenReturn(extensionElements);
    when(extensionElements.getElementsQuery()).thenReturn(query);
    when(query.filterByType(any(Class.class))).thenReturn(query);
    when(query.list())
        .thenReturn(Collections.emptyList(), Collections.emptyList(), List.of(taskDefinition));
    when(taskDefinition.getRetries()).thenReturn("7");

    ServiceTaskNode node = new ServiceTaskNode("n1", "teams");
    new MSTeamConnectorParser().parse(flowNode, node);

    assertEquals("7", node.getProperties().get("retries"));
  }

  @Test
  @DisplayName("parse leaves retries unset when no task definition")
  @SuppressWarnings("unchecked")
  void parse_withoutRetries() {
    Query<ModelElementInstance> query = mock(Query.class);
    when(flowNode.getExtensionElements()).thenReturn(extensionElements);
    when(extensionElements.getElementsQuery()).thenReturn(query);
    when(query.filterByType(any(Class.class))).thenReturn(query);
    when(query.list())
        .thenReturn(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    ServiceTaskNode node = new ServiceTaskNode("n1", "teams");
    new MSTeamConnectorParser().parse(flowNode, node);

    assertFalse(node.getProperties().containsKey("retries"));
  }
}

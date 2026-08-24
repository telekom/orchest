package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ProcessDefinition} node graph traversal and builder defaults. */
class ProcessDefinitionTest {

  private ProcessDefinition definition;

  @BeforeEach
  void setUp() {
    definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .version(1)
            .name("Test Process")
            .startNodeId("start-1")
            .build();
  }

  private TaskNode createTaskNode(String id, String name) {
    return new TaskNode(id, name);
  }

  @Nested
  class GetNode {

    @Test
    void getNode_existingNode_shouldReturnPresent() {
      TaskNode task = createTaskNode("task-1", "Task One");
      definition.getNodes().put("task-1", task);

      Optional<BaseNode> result = definition.getNode("task-1");

      assertTrue(result.isPresent());
      assertEquals("task-1", result.get().getId());
      assertEquals("Task One", result.get().getName());
    }

    @Test
    void getNode_nonExistentNode_shouldReturnEmpty() {
      Optional<BaseNode> result = definition.getNode("non-existent");

      assertTrue(result.isEmpty());
    }

    @Test
    void getNode_nullNodeId_shouldReturnEmpty() {
      Optional<BaseNode> result = definition.getNode(null);

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  class GetNodes {

    @Test
    void getNodes_initiallyEmpty() {
      assertTrue(definition.getNodes().isEmpty());
    }

    @Test
    void getNodes_afterAdding_shouldReturnAll() {
      definition.getNodes().put("task-1", createTaskNode("task-1", "Task One"));
      definition.getNodes().put("task-2", createTaskNode("task-2", "Task Two"));

      assertEquals(2, definition.getNodes().size());
      assertTrue(definition.getNodes().containsKey("task-1"));
      assertTrue(definition.getNodes().containsKey("task-2"));
    }
  }

  @Nested
  class GetOutgoingNodes {

    @Test
    void getOutgoingNodes_singleOutgoing_shouldReturnTargetNode() {
      TaskNode start = createTaskNode("start-1", "Start");
      start.getOutgoingSequenceFlowIds().put("flow-1", "task-1");

      TaskNode task = createTaskNode("task-1", "Task One");

      definition.getNodes().put("start-1", start);
      definition.getNodes().put("task-1", task);

      List<BaseNode> outgoing = definition.getOutgoingNodes("start-1");

      assertEquals(1, outgoing.size());
      assertEquals("task-1", outgoing.getFirst().getId());
    }

    @Test
    void getOutgoingNodes_multipleOutgoing_shouldReturnAllTargets() {
      TaskNode gateway = createTaskNode("gw-1", "Gateway");
      gateway.getOutgoingSequenceFlowIds().put("flow-1", "task-1");
      gateway.getOutgoingSequenceFlowIds().put("flow-2", "task-2");

      TaskNode task1 = createTaskNode("task-1", "Task One");
      TaskNode task2 = createTaskNode("task-2", "Task Two");

      definition.getNodes().put("gw-1", gateway);
      definition.getNodes().put("task-1", task1);
      definition.getNodes().put("task-2", task2);

      List<BaseNode> outgoing = definition.getOutgoingNodes("gw-1");

      assertEquals(2, outgoing.size());
      Set<String> ids = new HashSet<>();
      outgoing.forEach(n -> ids.add(n.getId()));
      assertTrue(ids.contains("task-1"));
      assertTrue(ids.contains("task-2"));
    }

    @Test
    void getOutgoingNodes_noOutgoing_shouldReturnEmptyList() {
      TaskNode endNode = createTaskNode("end-1", "End");
      // No outgoing sequence flows set

      definition.getNodes().put("end-1", endNode);

      List<BaseNode> outgoing = definition.getOutgoingNodes("end-1");

      assertTrue(outgoing.isEmpty());
    }

    @Test
    void getOutgoingNodes_targetNodeNotInDefinition_shouldSkip() {
      TaskNode start = createTaskNode("start-1", "Start");
      start.getOutgoingSequenceFlowIds().put("flow-1", "missing-node");

      definition.getNodes().put("start-1", start);

      List<BaseNode> outgoing = definition.getOutgoingNodes("start-1");

      assertTrue(outgoing.isEmpty());
    }
  }

  @Nested
  class GetIncomingNodes {

    @Test
    void getIncomingNodes_singleIncoming_shouldReturnSourceNode() {
      TaskNode task = createTaskNode("task-1", "Task One");
      task.getIncomingSequenceFlowIds().put("flow-1", "start-1");

      TaskNode start = createTaskNode("start-1", "Start");

      definition.getNodes().put("task-1", task);
      definition.getNodes().put("start-1", start);

      List<BaseNode> incoming = definition.getIncomingNodes("task-1");

      assertEquals(1, incoming.size());
      assertEquals("start-1", incoming.getFirst().getId());
    }

    @Test
    void getIncomingNodes_noIncoming_shouldReturnEmptyList() {
      TaskNode start = createTaskNode("start-1", "Start");
      definition.getNodes().put("start-1", start);

      List<BaseNode> incoming = definition.getIncomingNodes("start-1");

      assertTrue(incoming.isEmpty());
    }
  }

  @Nested
  class GetStartNode {

    @Test
    void getStartNode_shouldReturnNodeMatchingStartNodeId() {
      TaskNode start = createTaskNode("start-1", "Start Event");
      definition.getNodes().put("start-1", start);

      BaseNode startNode = definition.getStartNode();

      assertNotNull(startNode);
      assertEquals("start-1", startNode.getId());
      assertEquals("Start Event", startNode.getName());
    }

    @Test
    void getStartNode_noStartNodeInMap_shouldReturnNull() {
      // startNodeId is "start-1" but no node with that ID exists
      BaseNode startNode = definition.getStartNode();

      assertNull(startNode);
    }
  }

  @Nested
  class BuilderDefaults {

    @Test
    void builder_nodesDefaultsToEmptyMap() {
      ProcessDefinition pd = ProcessDefinition.builder().build();

      assertNotNull(pd.getNodes());
      assertTrue(pd.getNodes().isEmpty());
    }

    @Test
    void builder_sequenceFlowsDefaultsToEmptyMap() {
      ProcessDefinition pd = ProcessDefinition.builder().build();

      assertNotNull(pd.getSequenceFlows());
      assertTrue(pd.getSequenceFlows().isEmpty());
    }
  }
}

package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.bpmn.SequenceFlow;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Persistent model representing a deployed BPMN process definition with its node graph. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinition {

  /** Unique document identifier. */
  private String id;

  /** BPMN process definition key (stable across versions). */
  private String definitionId;

  /** Deployment version number. */
  private Integer version;

  /** Human-readable process name. */
  private String name;

  /** Map of node ID to node object representing all BPMN elements. */
  // key: nodeId & value: Node
  @Builder.Default private Map<String, BaseNode> nodes = new HashMap<>();

  /** Map of sequence flow ID to sequence flow object. */
  // key: sequenceFlowId and value: SequenceFlow
  @Builder.Default private Map<String, SequenceFlow> sequenceFlows = new HashMap<>();

  /** Raw BPMN XML of the deployed definition. */
  private String definitionXML;

  /** Node ID of the start event for this process. */
  private String startNodeId;

  /**
   * Mirrors the {@code isExecutable} attribute on the BPMN {@code <bpmn:process>} element. {@code
   * null} means the deployment predates this field and should be treated as executable for backward
   * compatibility; {@code Boolean.FALSE} explicitly disables process invocation and start-event
   * registration.
   */
  private Boolean isExecutable;

  /** Whether this definition represents a compensation flow. */
  private boolean isCompensationFlow;

  /** Timestamp when this definition was deployed. */
  private OffsetDateTime createdAt;

  /**
   * Returns true when this definition may be invoked / its start events registered. Legacy
   * documents without the field (null) are treated as executable.
   */
  public boolean isExecutableOrLegacy() {
    return isExecutable == null || isExecutable;
  }

  /** Returns all outgoing nodes from the given node. */
  public List<BaseNode> getOutgoingNodes(String nodeId) {
    List<BaseNode> outgoingNodes = new ArrayList<>();
    Map<String, BaseNode> nodes = getNodes();
    BaseNode currentNode = nodes.get(nodeId);

    for (String outgoingNodeId : currentNode.getOutgoingSequenceFlowIds().values()) {
      if (nodes.containsKey(outgoingNodeId)) outgoingNodes.add(nodes.get(outgoingNodeId));
    }
    return outgoingNodes;
  }

  /**
   * Gets all incoming nodes for a given node ID. Resolves SequenceFlow objects to actual node
   * objects.
   */
  public List<BaseNode> getIncomingNodes(String nodeId) {
    List<BaseNode> incomingNodes = new ArrayList<>();
    Map<String, BaseNode> nodes = getNodes();
    BaseNode currentNode = nodes.get(nodeId);

    for (String incomingNodeId : currentNode.getIncomingSequenceFlowIds().values()) {
      if (nodes.containsKey(incomingNodeId)) incomingNodes.add(nodes.get(incomingNodeId));
    }
    return incomingNodes;
  }

  /**
   * Retrieves a node by ID, returning empty if not found.
   *
   * @param nodeId the node identifier to look up
   * @return an Optional containing the node, or empty if absent
   */
  public Optional<BaseNode> getNode(String nodeId) {
    return Optional.ofNullable(getNodes().get(nodeId));
  }

  /**
   * Returns the start event node for this process definition.
   *
   * @return the start node
   */
  public BaseNode getStartNode() {
    return getNodes().get(getStartNodeId());
  }
}

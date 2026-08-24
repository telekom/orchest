package io.telekom.orchest.api.core.model.bpmn.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.ScopeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base abstract class representing a generic node in a BPMN process definition. All specific BPMN
 * element types (Tasks, Events, Gateways) extend this class.
 *
 * <p>It uses Jackson annotations for polymorphic deserialization based on the concrete class type.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TaskNode.class),
  @JsonSubTypes.Type(value = EventNode.class),
  @JsonSubTypes.Type(value = GatewayNode.class),
  @JsonSubTypes.Type(value = ActivityNode.class),
  @JsonSubTypes.Type(value = ServiceTaskNode.class),
  @JsonSubTypes.Type(value = UserTaskNode.class),
  @JsonSubTypes.Type(value = BusinessRuleTaskNode.class),
  @JsonSubTypes.Type(value = CallActivityNode.class),
  @JsonSubTypes.Type(value = SendTaskNode.class),
  @JsonSubTypes.Type(value = ReceiveTaskNode.class),
  @JsonSubTypes.Type(value = ScriptTaskNode.class),
  @JsonSubTypes.Type(value = ManualTaskNode.class),
  @JsonSubTypes.Type(value = SubProcessNode.class)
})
public abstract class BaseNode {
  /** Unique identifier for the node within the process definition. */
  private String id;

  /** Human-readable name of the node. */
  private String name;

  /** The type of the node (e.g., SERVICE_TASK, EXCLUSIVE_GATEWAY). */
  private NodeType type;

  /**
   * Constructs a new BaseNode with the specified ID, name, and type.
   *
   * @param id The unique identifier for the node.
   * @param name The human-readable name of the node.
   * @param type The type of the node.
   */
  protected BaseNode(String id, String name, NodeType type) {
    this.id = id;
    this.name = name;
    this.type = type;
  }

  // Scope information: identifies which process/subprocess contains this node
  /** The ID of the scope (Process or SubProcess) containing this node. */
  private String scopeId; // ID of the process or subprocess that contains this node

  /** The type of scope containing this node. */
  private ScopeType scopeType; // Type of scope (PROCESS, SUBPROCESS, EVENT_SUBPROCESS)

  /** Map of outgoing sequence flow IDs to target Node IDs. */
  private Map<String, String> outgoingSequenceFlowIds = new HashMap<>(); // sequenceId -> NodeId

  /** Map of incoming sequence flow IDs to source Node IDs. */
  private Map<String, String> incomingSequenceFlowIds = new HashMap<>(); // sequenceId -> NodeId

  // Input and output data mappings
  /** Data mappings for input variables. */
  private List<DataMapping> inputMappings = new ArrayList<>();

  /** Data mappings for output variables. */
  private List<DataMapping> outputMappings = new ArrayList<>();

  /** Additional properties associated with the node. */
  private Map<String, Object> properties = new HashMap<>();
}

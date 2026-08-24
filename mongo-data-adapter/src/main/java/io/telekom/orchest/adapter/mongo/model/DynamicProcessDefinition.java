package io.telekom.orchest.adapter.mongo.model;

import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a BPMN Process Definition. Stores the structure, version, and
 * content of a deployed process.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class DynamicProcessDefinition extends ProcessDefinition {

  /** The process instance ID that owns this dynamic definition variant. */
  private String processInstanceId;

  /** List of agent chat session IDs associated with this dynamic process. */
  private List<String> agentChats;

  /**
   * Creates a DynamicProcessDefinition from an existing ProcessDefinition with additional fields.
   */
  public DynamicProcessDefinition(
      ProcessDefinition pd, String processInstanceId, List<String> agentChats) {
    super(
        pd.getId(),
        pd.getDefinitionId(),
        pd.getVersion(),
        pd.getName(),
        pd.getNodes(),
        pd.getSequenceFlows(),
        pd.getDefinitionXML(),
        pd.getStartNodeId(),
        true,
        false,
        pd.getCreatedAt());
    this.processInstanceId = processInstanceId;
    this.agentChats = agentChats;
  }
}

package io.telekom.orchest.api.core.adapters.data.model;

import java.util.HashMap;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * A process definition variant that is dynamically created for a specific process instance,
 * supporting AI agent chat sessions.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DynamicProcessDefinition extends ProcessDefinition {

  /** The process instance this dynamic definition was created for. */
  private String processInstanceId;

  /** List of agent chat session identifiers associated with this dynamic process. */
  private List<String> agentChats;

  /**
   * Creates a DynamicProcessDefinition by copying fields from an existing ProcessDefinition.
   *
   * @param pd the source process definition to copy from
   * @return a new DynamicProcessDefinition with the same field values
   */
  public static DynamicProcessDefinition fromProcessDefinition(ProcessDefinition pd) {
    return DynamicProcessDefinition.builder()
        .id(pd.getId())
        .definitionId(pd.getDefinitionId())
        .version(pd.getVersion())
        .name(pd.getName())
        .nodes(pd.getNodes() != null ? new HashMap<>(pd.getNodes()) : new HashMap<>())
        .sequenceFlows(
            pd.getSequenceFlows() != null ? new HashMap<>(pd.getSequenceFlows()) : new HashMap<>())
        .definitionXML(pd.getDefinitionXML())
        .startNodeId(pd.getStartNodeId())
        .createdAt(pd.getCreatedAt())
        .build();
  }
}

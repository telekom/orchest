package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.model.bpmn.SequenceFlow;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a BPMN Process Definition. Stores the structure, version, and
 * content of a deployed process.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ProcessDefinition {

  /** Unique database identifier. */
  @Id private String id;

  /** The logical identifier of the process definition (e.g., from BPMN XML). */
  private String definitionId;

  /** The version of this process definition. */
  private Integer version;

  /** The human-readable name of the process. */
  private String name;

  /** Map of nodes in the process, keyed by node ID. */
  // key: nodeId & value: Node
  @Builder.Default private Map<String, BaseNode> nodes = new HashMap<>();

  /** Map of sequence flows in the process, keyed by flow ID. */
  // key: sequenceFlowId and value: SequenceFlow
  @Builder.Default private Map<String, SequenceFlow> sequenceFlows = new HashMap<>();

  /** The raw XML content of the process definition. */
  private String definitionXML;

  /** The ID of the start node for this process. */
  private String startNodeId;

  /** Whether the process is marked as executable in the BPMN XML. */
  private Boolean isExecutable;

  /** Whether this definition represents a compensation flow. */
  private boolean isCompensationFlow;

  /** Timestamp when this definition was created/deployed. */
  @CreatedDate private OffsetDateTime createdAt;
}

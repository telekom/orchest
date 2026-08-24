package io.telekom.orchest.api.core.model.bpmn.extensions;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Extension element representing a connector task configuration attached to a BPMN service task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Connector {

  /** Unique identifier for this connector instance. */
  private String id;

  /** Name identifying the connector type (e.g., "http", "kafka"). */
  private String connectorName;

  /** Configuration data specific to the connector type. */
  private Map<String, Object> connectorData;

  /** Timestamp when this connector configuration was created. */
  private OffsetDateTime createdAt;
}
